package com.example.demo.boardVo;

import java.io.File;
import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardFileDownVo {
	private int id;
	private String saveName;
	private String originalName;
	private String deleteyn;
	private Date inserttime;
	private Date deletetime;
	private int boardid;
}
