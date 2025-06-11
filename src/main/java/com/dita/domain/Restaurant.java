package com.dita.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
// Lombok 사용 시
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "restaurant", schema = "", indexes = @Index(name = "idx_member_id", columnList = "member_id"))
@Getter
@Setter
@NoArgsConstructor
@ToString
@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class Restaurant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "rst_id")
	@JsonProperty("rst_id")  
	private Integer rstId;

	@Column(name = "name", length = 100, nullable = false, columnDefinition = "VARCHAR(100) COLLATE euckr_korean_ci")
	private String name;

	@Column(name = "status", columnDefinition = "ENUM('대기','승인') COLLATE euckr_korean_ci")
	private String status;

	@Column(name = "intro", length = 500, columnDefinition = "VARCHAR(500) COLLATE euckr_korean_ci")
	private String intro;

	@Column(name = "address", length = 255, nullable = false, columnDefinition = "VARCHAR(255) COLLATE euckr_korean_ci")
	private String address;

	@Column(name = "phone", length = 50, nullable = false, columnDefinition = "VARCHAR(50) COLLATE euckr_korean_ci")
	private String phone;

	@Column(name = "rating", precision = 2, scale = 1)
	private BigDecimal rating;

	@Column(name = "latitude", precision = 11, scale = 8, nullable = false)
    @JsonAlias("lat")
	private BigDecimal latitude;

	@Column(name = "longitude", precision = 11, scale = 8, nullable = false)
	@JsonAlias({"lng", "lon"})
	private BigDecimal longitude;

	@Column(name = "tag", length = 1500, columnDefinition = "VARCHAR(1500) COLLATE euckr_korean_ci")
	private String tag;

	@Column(name = "region_label", length = 50, nullable = false, columnDefinition = "VARCHAR(50) COLLATE euckr_korean_ci")
	private String regionLabel;

	@Column(name = "region_2label", length = 50, nullable = false, columnDefinition = "VARCHAR(50) COLLATE euckr_korean_ci")
	private String region2Label;

	@Column(name = "image", length = 500, nullable = false, columnDefinition = "VARCHAR(500) COLLATE euckr_korean_ci")
	private String image;

	@Column(name = "jjim_count")
	private Integer jjimCount;

	@Column(name = "created_at", insertable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdAt;

	@Column(name = "member_id", length = 50, nullable = false, columnDefinition = "VARCHAR(50) COLLATE euckr_korean_ci")
	private String memberId;

	// (선택) 만약 Member 엔티티가 있다면 아래처럼 ManyToOne 매핑 가능
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", referencedColumnName = "member_id", insertable = false, updatable = false)
	private Member member;
}
