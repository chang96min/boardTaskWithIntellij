package com.example.demo.boardController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import com.example.demo.boardVo.BoardFileDownVo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.boardDto.BoardDto;
import com.example.demo.boardPaging.BoardPaging;
import com.example.demo.boardService.BoardService;
import com.example.demo.boardVo.BoardDetVo;
import com.example.demo.boardVo.BoardEntCountVo;
import com.example.demo.webSecurity.TokenDto;
import com.example.demo.webSecurity.UserRepository;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
//협업 관련 - 다른 ip에서 접속 허용
@CrossOrigin(origins = "*", allowedHeaders ="*")
public class BoardController {
	
	public final BoardService boardService;
	public final UserRepository userRepository;
	
	//게시판 전체글 리스트 출력
	@GetMapping("/user/board")
	@CrossOrigin(origins = "*", allowedHeaders ="*")
	public BoardEntCountVo boardEntList(BoardPaging boardPaging) {
		
		BoardEntCountVo vo = new BoardEntCountVo();
	
		vo.setBoardInfo(boardService.boardInfo(boardPaging));
		vo.setTotalNum(boardService.boardCount(boardPaging));
		
		return vo;	
	}
	
	//게시글 상세 페이지 출력
	@GetMapping("/user/boardDet")
	public BoardDetVo boardDet(BoardDto boardDto) {
		
		int boardId = boardDto.getId();

		BoardDetVo vo = new BoardDetVo();
		
		vo.setBoardDetInfoVo(boardService.boardDetInfo(boardId));
		vo.setBoardDetFileVo(boardService.boardDetFile(boardId));
		
		return vo;
	}
	
	
//	//게시글 수정 페이지
//	@GetMapping("/boardModPage")
//	public List<BoardDetVo> boardModPage(@RequestBody BoardDto boardDto) {
//		
//		int boardId = boardDto.getId();
//		
//		return boardService.boardDet(boardId);
//	}
	
	//게시글 수정
	@PostMapping("/user/boardMod")
	public String boardMod(@ModelAttribute BoardDto boardDto) throws IllegalStateException, IOException{
		
		//첨부파일이 있으면
				if(boardDto.getFile() != null) {
					//게시글 수정
					boardService.boardMod(boardDto);
					//첨부파일 여러개일 경우 반복하여 각각 저장
					for(MultipartFile file : boardDto.getFile()) {
						UUID uuid = UUID.randomUUID();
					    String originalName = file.getOriginalFilename();
					    String saveName = uuid + originalName;
						File dest = new File("C:\\saveFile\\"+saveName);
						file.transferTo(dest);
						String dir = "C:\\saveFile\\"+saveName;
					
						
					//첨부파일 등록
					boardService.boardModFile(originalName,dir,saveName,boardDto);
					    }
				//첨부파일이 없으면
				} else {
					//게시글 수정
					boardService.boardMod(boardDto);
				}
		
		return "redirect:/board";
	}
	
	//게시글 등록 페이지
	@GetMapping("/user/boardAddPage")
	public void boarAdd() {
		
	}
	
	//게시글 등록
	@PostMapping("/user/boardAdd")
	public String boardAdd(@ModelAttribute BoardDto boardDto) throws IllegalStateException, IOException {
		
		//토큰에서 정보를 가져오는 부분
		String principal = SecurityContextHolder.getContext().getAuthentication().getName();
		TokenDto tokenDto = userRepository.find(principal);
		
		//게시글 등록
		boardService.boardAdd(boardDto, tokenDto);
		
		//첨부파일이 있으면
		if(boardDto.getFile() != null) {
			
			//첨부파일 여러개일 경우 반복하여 각각 저장
			for(MultipartFile file : boardDto.getFile()) {
				UUID uuid = UUID.randomUUID();
			    String originalName = file.getOriginalFilename();
			    String saveName = uuid + originalName;
				File dest = new File("C:\\saveFile\\"+saveName);
				file.transferTo(dest);
				String dir = "C:\\saveFile\\"+saveName;
			
				//첨부파일 등록
				boardService.boardFile(originalName,dir,saveName);
			    }
		} 
		return "redirect:/board";	
		
	}
	
	//게시글 삭제
	@PostMapping("/user/boardDel")
	public void boardDel(@RequestBody BoardDto boardDto) {
		int id = boardDto.getId();
		boardService.boardDel(id);
	}

	//파일 다운로드
	@GetMapping("/user/board/filedownload/{id}")
	public ResponseEntity<Resource> BoardFileDownload(@PathVariable int id) throws IOException {
		//file 정보를 불러옴
		BoardFileDownVo file = boardService.getfile(id);
		//파일 이름 가져오기
		String fileName = file.getSaveName();
		//파일 이름과 경로 합치기
		StringBuilder sb = new StringBuilder("C:\\saveFile\\");
		sb.append(fileName);
		//파일 저장경로와 이름 가져오기
		String fileSaveName = sb.toString();
		//파일 리소스 얻기
		Resource resource = new InputStreamResource(Files.newInputStream(Path.of(fileSaveName)));

//		File saveFile = new File(fileSaveName);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentDisposition(ContentDisposition.builder("attachment").filename(file.getOriginalName()).build());


		return new ResponseEntity<>(resource, headers, HttpStatus.OK);
//		return saveFile;
	}

	
		

}
