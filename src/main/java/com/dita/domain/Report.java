package com.dita.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "REPORT")
public class Report {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "report_id") // DB 컬럼명은 그대로 유지 가능
	private int reportId;

    @Enumerated(EnumType.STRING)
    private ReportType type;
    
    @Column(name = "target_id") // DB 컬럼명과 매핑
    private Integer targetId;

    private String reason;

    @CreationTimestamp
    @Column(name = "reported_at", updatable = false)
    private LocalDateTime reportedAt;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @ManyToOne
    @JoinColumn(name = "reporter_member_id")
    private Member reporterMember;

    @ManyToOne
    @JoinColumn(name = "reported_member_id")
    private Member reportedMember;

}
