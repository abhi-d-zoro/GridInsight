package com.gridinsight.backend.SERM_6.dto;

import com.gridinsight.backend.SERM_6.entity.SustainabilityMetric;
import java.util.List;

public class DashboardSummary {
    private double avgRenewableShare;
    private double totalEmissionsAvoided;
    private List<SustainabilityMetric> drillDownMetrics;

    public DashboardSummary(double avgRenewableShare, double totalEmissionsAvoided,
                            List<SustainabilityMetric> drillDownMetrics) {
        this.avgRenewableShare = avgRenewableShare;
        this.totalEmissionsAvoided = totalEmissionsAvoided;
        this.drillDownMetrics = drillDownMetrics;
    }

    // Getters and Setters
    public double getAvgRenewableShare() { return avgRenewableShare; }
    public void setAvgRenewableShare(double avgRenewableShare) { this.avgRenewableShare = avgRenewableShare; }

    public double getTotalEmissionsAvoided() { return totalEmissionsAvoided; }
    public void setTotalEmissionsAvoided(double totalEmissionsAvoided) { this.totalEmissionsAvoided = totalEmissionsAvoided; }

    public List<SustainabilityMetric> getDrillDownMetrics() { return drillDownMetrics; }
    public void setDrillDownMetrics(List<SustainabilityMetric> drillDownMetrics) { this.drillDownMetrics = drillDownMetrics; }
}
