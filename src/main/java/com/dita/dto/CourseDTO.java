package com.dita.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDTO {

    private int courseId;
    private String title;
    private String intro;
    private String tag;
    private String restaurants;
    private int jjimCount;
    private int likes;
    private String image;
    private String createAt;  // DateTime 타입은 String으로 전달될 경우 포맷을 맞추는 것이 좋습니다.
    
    private boolean jjimmed; // 로그인한 회원이 찜한 상태인지 여부
    
    public CourseDTO(int courseId, String title, String intro, String tag, String restaurants,
	            int jjimCount, int likes, String image, String createAtFormatted) {
	this.courseId = courseId;
	this.title = title;
	this.intro = intro;
	this.tag = tag;
	this.restaurants = restaurants;
	this.jjimCount = jjimCount;
	this.likes = likes;
	this.image = image;
	this.createAt = createAtFormatted;
	}

}
