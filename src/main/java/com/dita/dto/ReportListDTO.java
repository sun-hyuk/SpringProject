package com.dita.dto;

import java.time.LocalDateTime;

import com.dita.domain.ReportStatus;
import com.dita.domain.ReportType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportListDTO {
    private int report_id;
    private ReportStatus status;
    private ReportType type;
    private String reporterNickname;
    private String reportedNickname;
    private LocalDateTime reportedAt;

    // ✅ 템플릿에서 사용할 한글 문자열 getter
    public String getReportType() {
        return type != null ? type.name() : "";
    }

    public String getStatusName() {
        return status != null ? status.name() : "";
    }
    
    public int getId() {
        return report_id;
    }
}
