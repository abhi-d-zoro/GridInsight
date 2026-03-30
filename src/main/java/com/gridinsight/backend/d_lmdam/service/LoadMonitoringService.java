package com.gridinsight.backend.d_lmdam.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gridinsight.backend.d_lmdam.dto.*;
import com.gridinsight.backend.d_lmdam.entity.DemandType;
import com.gridinsight.backend.d_lmdam.entity.LoadRecord;
import com.gridinsight.backend.d_lmdam.entity.PeakEvent;
import com.gridinsight.backend.d_lmdam.entity.Severity;
import com.gridinsight.backend.d_lmdam.repository.LoadRecordRepository;
import com.gridinsight.backend.d_lmdam.repository.PeakEventRepository;
import com.gridinsight.backend.z_common.NotFoundException;
import com.gridinsight.backend.z_common.audit.AuditLog;
import com.gridinsight.backend.z_common.audit.AuditLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoadMonitoringService {

    private final LoadRecordRepository loadRepo;
    private final PeakEventRepository peakRepo;
    private final AuditLogRepository auditRepo;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private GenerationDataProvider generationProvider;

    private static final Duration ROLLING_WINDOW = Duration.ofMinutes(15);
    private static final double DEFAULT_ZONE_THRESHOLD_MW = 250.0;
    private static final double DEFAULT_DEFICIT_THRESHOLD_MW = 50.0;

    // ============================================================
    // CREATE LOAD RECORD
    // ============================================================
    public LoadRecordResponse createLoadRecord(LoadRecordCreateRequest req, Long actorUserId, String ip) {

        if (req.demandMW() < 0) {
            throw new IllegalArgumentException("demandMW must be >= 0");
        }

        Optional<LoadRecord> existingOpt =
                loadRepo.findByZoneIdAndTimestamp(req.zoneId(), req.timestamp());

        LoadRecord saved;

        if (existingOpt.isPresent()) {
            LoadRecord existing = existingOpt.get();
            boolean changed = false;

            if (existing.getDemandMW() != req.demandMW()) {
                existing.setDemandMW(req.demandMW());
                changed = true;
            }

            if (existing.getDemandType() != req.demandType()) {
                existing.setDemandType(req.demandType());
                changed = true;
            }

            saved = loadRepo.save(existing);

            if (changed) {
                audit("LOAD_RECORD_UPDATED", "load/records", actorUserId, saved.getLoadId(),
                        meta("zoneId", saved.getZoneId(),
                                "demandMW", saved.getDemandMW(),
                                "demandType", saved.getDemandType()), ip);
            }

        } else {
            LoadRecord entity = LoadRecord.builder()
                    .zoneId(req.zoneId())
                    .timestamp(req.timestamp())
                    .demandMW(req.demandMW())
                    .demandType(req.demandType())
                    .build();

            saved = loadRepo.save(entity);

            audit("LOAD_RECORD_CREATED", "load/records", actorUserId, saved.getLoadId(),
                    meta("zoneId", saved.getZoneId(),
                            "demandMW", saved.getDemandMW(),
                            "demandType", saved.getDemandType()), ip);
        }

        checkAndRaisePeakEvent(saved.getZoneId(), saved.getTimestamp(), actorUserId, ip);

        return map(saved);
    }

    // ============================================================
    // LIST LOAD RECORDS
    // ============================================================
    public Page<LoadRecordResponse> listLoadRecords(
            String zoneId, Instant from, Instant to,
            EnumSet<DemandType> types, Pageable pageable) {

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("Invalid window: from must be <= to");
        }

        Page<LoadRecord> page;

        if (zoneId != null && from != null && to != null && types != null && !types.isEmpty()) {
            page = loadRepo.findByZoneIdAndTimestampBetweenAndDemandTypeIn(zoneId, from, to, types, pageable);

        } else if (zoneId != null && from != null && to != null) {
            page = loadRepo.findByZoneIdAndTimestampBetween(zoneId, from, to, pageable);

        } else if (zoneId != null) {
            page = loadRepo.findByZoneId(zoneId, pageable);

        } else {
            page = loadRepo.findAll(pageable);
        }

        return page.map(this::map);
    }

    // ============================================================
    // GET LOAD RECORD
    // ============================================================
    public LoadRecordResponse getLoadRecord(Long id) {
        LoadRecord lr = loadRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Load record not found: " + id));
        return map(lr);
    }

    // ============================================================
    // UPDATE LOAD RECORD
    // ============================================================
    public LoadRecordResponse updateLoadRecord(Long id, LoadRecordUpdateRequest req,
                                               Long actorUserId, String ip) {

        LoadRecord lr = loadRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Load record not found: " + id));

        Map<String, Object> changed = new LinkedHashMap<>();

        if (req.timestamp() != null && !Objects.equals(req.timestamp(), lr.getTimestamp())) {
            lr.setTimestamp(req.timestamp());
            changed.put("timestamp", req.timestamp());
        }

        if (req.demandMW() != null && req.demandMW() >= 0 &&
                !Objects.equals(req.demandMW(), lr.getDemandMW())) {
            lr.setDemandMW(req.demandMW());
            changed.put("demandMW", req.demandMW());
        }

        if (req.demandType() != null && !Objects.equals(req.demandType(), lr.getDemandType())) {
            lr.setDemandType(req.demandType());
            changed.put("demandType", req.demandType().name());
        }

        LoadRecord saved = loadRepo.save(lr);

        if (!changed.isEmpty()) {
            audit("LOAD_RECORD_UPDATED", "load/records", actorUserId, id,
                    asJson(changed), ip);

            checkAndRaisePeakEvent(saved.getZoneId(), saved.getTimestamp(), actorUserId, ip);
        }

        return map(saved);
    }

    // ============================================================
    // DELETE LOAD RECORD
    // ============================================================
    public void deleteLoadRecord(Long id, Long actorUserId, String ip) {

        LoadRecord lr = loadRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Load record not found: " + id));

        loadRepo.delete(lr);

        audit("LOAD_RECORD_DELETED", "load/records", actorUserId, id,
                meta("zoneId", lr.getZoneId()), ip);
    }

    // ============================================================
    // CREATE PEAK EVENT
    // ============================================================
    public PeakEventResponse createPeak(PeakEventCreateRequest req, Long actorUserId, String ip) {

        if (req.peakMW() < 0)
            throw new IllegalArgumentException("peakMW must be >= 0");

        if (req.startTime().isAfter(req.endTime()))
            throw new IllegalArgumentException("startTime must be <= endTime");

        PeakEvent pe = PeakEvent.builder()
                .zoneId(req.zoneId())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .peakMW(req.peakMW())
                .severity(req.severity())
                .build();

        PeakEvent saved = peakRepo.save(pe);

        audit("PEAK_EVENT_CREATED", "load/peaks", actorUserId, saved.getPeakId(),
                meta("zoneId", saved.getZoneId(),
                        "peakMW", saved.getPeakMW(),
                        "severity", saved.getSeverity()), ip);

        return map(saved);
    }

    // ============================================================
    // LIST PEAK EVENTS  ✅ THIS WAS THE FINAL BROKEN METHOD
    // ============================================================
    public Page<PeakEventResponse> listPeaks(String zoneId, Severity severity, Pageable pageable) {

        Page<PeakEvent> page;

        if (zoneId != null && severity != null) {
            page = peakRepo.findByZoneIdAndSeverity(zoneId, severity, pageable);

        } else if (zoneId != null) {
            page = peakRepo.findByZoneId(zoneId, pageable);

        } else {
            page = peakRepo.findAll(pageable);
        }

        return page.map(this::map);
    }

    // ============================================================
    // GET PEAK EVENT
    // ============================================================
    public PeakEventResponse getPeak(Long id) {
        PeakEvent pe = peakRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Peak event not found: " + id));
        return map(pe);
    }

    // ============================================================
    // UPDATE PEAK EVENT
    // ============================================================
    public PeakEventResponse updatePeak(Long id, PeakEventUpdateRequest req,
                                        Long actorUserId, String ip) {

        PeakEvent pe = peakRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Peak event not found: " + id));

        if (req.startTime() != null) pe.setStartTime(req.startTime());
        if (req.endTime() != null) pe.setEndTime(req.endTime());
        if (req.peakMW() != null) pe.setPeakMW(req.peakMW());
        if (req.severity() != null) pe.setSeverity(req.severity());

        PeakEvent saved = peakRepo.save(pe);

        audit("PEAK_EVENT_UPDATED", "load/peaks", actorUserId,
                saved.getPeakId(), "{}", ip);

        return map(saved);
    }

    // ============================================================
    // DELETE PEAK EVENT
    // ============================================================
    public void deletePeak(Long id, Long actorUserId, String ip) {

        PeakEvent pe = peakRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Peak event not found: " + id));

        peakRepo.delete(pe);

        audit("PEAK_EVENT_DELETED", "load/peaks", actorUserId, id,
                meta("zoneId", pe.getZoneId()), ip);
    }

    // ============================================================
    // AUTOMATED PEAK EVENT DETECTION
    // ============================================================
    private void checkAndRaisePeakEvent(String zoneId, Instant ts, Long actorUserId, String ip) {

        Instant from = ts.minus(ROLLING_WINDOW);
        Instant to = ts;

        Double max = loadRepo.findMaxDemandInWindow(zoneId, from, to);
        if (max == null) return;

        double threshold = getZoneThresholdMW(zoneId);
        if (max <= threshold) return;

        double excess = ((max - threshold) / threshold) * 100.0;
        Severity severity = mapSeverity(excess);

        PeakEvent toSave = peakRepo.findTopByZoneIdOrderByEndTimeDesc(zoneId)
                .filter(last -> !last.getEndTime().isBefore(from))
                .map(last -> {

                    if (max > last.getPeakMW()) last.setPeakMW(max);
                    if (severityRank(severity) > severityRank(last.getSeverity()))
                        last.setSeverity(severity);

                    if (to.isAfter(last.getEndTime())) last.setEndTime(to);

                    return last;
                })
                .orElseGet(() -> PeakEvent.builder()
                        .zoneId(zoneId)
                        .startTime(from)
                        .endTime(to)
                        .peakMW(max)
                        .severity(severity)
                        .build());

        boolean creating = (toSave.getPeakId() == null);

        PeakEvent saved = peakRepo.save(toSave);

        audit(creating ? "PEAK_EVENT_CREATED" : "PEAK_EVENT_UPDATED",
                "load/peaks",
                actorUserId,
                saved.getPeakId(),
                meta("zoneId", saved.getZoneId(),
                        "peakMW", saved.getPeakMW(),
                        "severity", saved.getSeverity()),
                ip);
    }

    private Severity mapSeverity(double excessPct) {
        if (excessPct > 20) return Severity.HIGH;
        if (excessPct > 10) return Severity.MEDIUM;
        return Severity.LOW;
    }

    private int severityRank(Severity s) {
        return switch (s) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
        };
    }

    private double getZoneThresholdMW(String zoneId) {
        return DEFAULT_ZONE_THRESHOLD_MW;
    }

    // ============================================================
    // OVERLAY
    // ============================================================
    public OverlayResponse getOverlay(String zoneId, Instant from, Instant to, Granularity granularity) {

        if (zoneId == null)
            throw new IllegalArgumentException("zoneId is required");

        if (from == null || to == null)
            throw new IllegalArgumentException("from/to are required");

        if (from.isAfter(to))
            throw new IllegalArgumentException("'from' must be <= 'to'");

        Granularity g = (granularity != null) ? granularity : Granularity.MIN_5;

        List<LoadRecord> loadSeries =
                loadRepo.findByZoneIdAndTimestampBetweenOrderByTimestamp(zoneId, from, to);

        Map<Instant, Double> genByBucket;

        if (generationProvider != null) {

            var genSeries = generationProvider.fetchGenerationSeries(Long.valueOf(zoneId), from, to);

            genByBucket = genSeries.stream()
                    .collect(Collectors.groupingBy(
                            p -> bucket(g, p.ts()),
                            Collectors.summingDouble(GenerationDataProvider.GenerationPoint::generatedMW)
                    ));

        } else {
            genByBucket = Collections.emptyMap();
        }

        Map<Instant, Double> demandByBucket = loadSeries.stream()
                .collect(Collectors.groupingBy(
                        lr -> bucket(g, lr.getTimestamp()),
                        Collectors.summingDouble(LoadRecord::getDemandMW)
                ));

        List<OverlayPoint> points = demandByBucket.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {

                    Instant bucket = e.getKey();
                    double demand = e.getValue();
                    double generated = genByBucket.getOrDefault(bucket, 0.0);

                    double deficit = Math.max(demand - generated, 0.0);
                    boolean red = deficit > DEFAULT_DEFICIT_THRESHOLD_MW;

                    return new OverlayPoint(bucket, demand, generated, deficit, red);
                })
                .toList();

        return new OverlayResponse(zoneId, g, DEFAULT_DEFICIT_THRESHOLD_MW, points);
    }

    private Instant bucket(Granularity g, Instant ts) {
        long epochSeconds = ts.getEpochSecond();
        long size = g.seconds;
        return Instant.ofEpochSecond((epochSeconds / size) * size);
    }

    // ============================================================
    // MAPPERS + UTILITIES
    // ============================================================
    private LoadRecordResponse map(LoadRecord e) {
        return new LoadRecordResponse(
                e.getLoadId(),
                e.getZoneId(),
                e.getTimestamp(),
                e.getDemandMW(),
                e.getDemandType(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    private PeakEventResponse map(PeakEvent e) {
        return new PeakEventResponse(
                e.getPeakId(),
                e.getZoneId(),
                e.getStartTime(),
                e.getEndTime(),
                e.getPeakMW(),
                e.getSeverity(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    private String meta(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2)
            map.put(kv[i].toString(), kv[i + 1]);
        return asJson(map);
    }

    private String asJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private void audit(String action, String resource, Long actorUserId,
                       Long targetId, String metadata, String ip) {

        AuditLog log = AuditLog.builder()
                .actorUserId(actorUserId)
                .targetUserId(targetId)
                .action(action)
                .resource(resource)
                .timestamp(Instant.now())
                .metadata(metadata)
                .ipAddress(ip)
                .correlationId(UUID.randomUUID().toString())
                .build();

        auditRepo.save(log);
    }

    // ============================================================
    // Nested DTOs
    // ============================================================
    public enum Granularity {
        MIN_1(60),
        MIN_5(300),
        MIN_15(900);

        public final long seconds;

        Granularity(long s) {
            this.seconds = s;
        }
    }

    public record OverlayPoint(Instant ts, double demandMW,
                               double generatedMW, double deficitMW, boolean red) {}

    public record OverlayResponse(String zoneId, Granularity granularity,
                                  double thresholdMW, List<OverlayPoint> points) {}

}