package com.gridinsight.backend.f_serm.dto;

import java.util.List;

public class DashboardSummary {

    private double avgRenewableShare;
    private double totalEmissionsAvoided;
    private List<SustainabilityMetricDTO> drillDownMetrics;

    public DashboardSummary(double avgRenewableShare,
                            double totalEmissionsAvoided,
                            List<SustainabilityMetricDTO> drillDownMetrics) {

        this.avgRenewableShare = avgRenewableShare;
        this.totalEmissionsAvoided = totalEmissionsAvoided;
        this.drillDownMetrics = drillDownMetrics;
    }

    public double getAvgRenewableShare() { return avgRenewableShare; }
    public void setAvgRenewableShare(double avgRenewableShare) { this.avgRenewableShare = avgRenewableShare; }

    public double getTotalEmissionsAvoided() { return totalEmissionsAvoided; }
    public void setTotalEmissionsAvoided(double totalEmissionsAvoided) { this.totalEmissionsAvoided = totalEmissionsAvoided; }

    public List<SustainabilityMetricDTO> getDrillDownMetrics() { return drillDownMetrics; }
    public void setDrillDownMetrics(List<SustainabilityMetricDTO> drillDownMetrics) { this.drillDownMetrics = drillDownMetrics; }
}