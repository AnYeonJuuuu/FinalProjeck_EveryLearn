package com.coding5.el.class_comm.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coding5.el.class_comm.service.ClassCommService;
import com.coding5.el.class_comm.vo.ClassCommVo;
import com.coding5.el.class_comm.vo.CommentVo;
import com.coding5.el.common.page.PageVo;
import com.coding5.el.common.page.Pagination;
import com.google.gson.Gson;
//import com.sun.tools.classfile.InnerClasses_attribute.Info;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//@RequestMapping("class")
@RequestMapping(value = "class", produces = "application/text; charset=utf8")
@Controller
public class ClassCommController {
	
	@Autowired
	private ClassCommService ccs;
	
	@Autowired
	private Gson gson;
	
	@GetMapping("writeModify")
	public String writeModify(String cNo, Model model) {
		
		ClassCommVo modifyData = ccs.modifyData(cNo);
		model.addAttribute("modifyData", modifyData);
		
		return "class_comm/write_modify";
	}
	
	@PostMapping("writeModify")
	public String writeModify(ClassCommVo classVo) {
		
		log.info("classVo :::" + classVo.getCateNo());
		int result = ccs.modify(classVo);
		
		if(classVo.getCateNo().equals("1")) {
			return "redirect:/class/qna";
		}else if(classVo.getCateNo().equals("2")) {
			return "redirect:/class/study";
		}else if (classVo.getCateNo().equals("3")){
			return "redirect:/class/free";
		}else {
			return "";
		}
		
		
	}
	
	//likeup ajax
	@PostMapping("likeupAjax")
	@ResponseBody
	public String likeupAjax(String memberNo, String classCommNo, HashMap<String, String> likeupMap) {
		 
		likeupMap.put("memberNo", memberNo);
		likeupMap.put("classCommNo", classCommNo);
		
		
		// 0 이면 빨간하트 , 1이면 다시 검은 하트로 // 빨간하트 유지 == 디테일 페이지에서? 데이터 다시 가져와서 처리??
		int likeCheckInt = ccs.likeCheck(likeupMap);
		log.info("likeCheck결과 :: " + likeCheckInt);
		String likeCheck = Integer.toString(likeCheckInt);
		
		likeupMap.put("likeCheck", likeCheck);
		
		
		//좋아요 카운트 셀렉트
		String likeCntAjax = ccs.likeCntAjax(classCommNo);
		likeupMap.put("likeCntAjax", likeCntAjax);
		
//		String s = gson.toJson(r);
		log.info("likeupMap" + likeupMap);
		
		return gson.toJson(likeupMap);
	}
	
	//신고 성공페이지
	@GetMapping("reportSuccess")
	public String reportSuccess() {
		return "class_comm/reportSuccess";
	}
	
	//게시글 삭제 (에이젝스)
	@PostMapping("deleteAjax")
	public String deleteAjax(String classCommNo) {
		
		int result = ccs.deleteWrite(classCommNo);
		
		log.info("result ::" + result);
		
		
		return "class_comm/qna";
	}

	//게시글 등록(화면)
	@GetMapping("write")
	public String write() {
		return "class_comm/comm_write";
	}
	
	//게시글 등록
	@PostMapping("write")
	public String write(ClassCommVo vo) {
		
		int result = ccs.write(vo);
		
		if(result != 1) {
			return "common/error";
		}
		
		return "redirect:/class/qna";
	}
	
	//질답 화면
	@GetMapping("qna")
	public String qna(Model model) {
		
		List<ClassCommVo> qnaList = ccs.qnaList();
		
		//댓글 갯수 카운트
		List<ClassCommVo> qnaCommentList = ccs.qnaCommentList(qnaList);
		log.info("qnaCommentList :: " + qnaCommentList);
		model.addAttribute("qnaCommentList", qnaCommentList);
		
		
		
		log.info("큐앤에이 리스트" + qnaList);
		model.addAttribute("qnaList", qnaList);
		
		
		
		if(qnaList == null) {
			return "common/error";
		}
		
		return "class_comm/qna";
	}
	
	//질문과 답변
	@PostMapping("qna")
	public String qna() {
		

		
		return "class_comm/qna";
	}
	
	//게시글 상세(화면)
	@GetMapping("detail")
	public String detail(String classCommNo, String lc, Model model, HashMap<String, String> likeMap) {
		
		//조회수 증가
		int result = ccs.increaseHit(classCommNo);
		
		log.info("lc ::" + lc);
		likeMap.put("classCommNo", classCommNo);
		likeMap.put("memberNo", lc);
		log.info("likeMap ::" + likeMap);
		
		ClassCommVo detailVo = ccs.detailVo(likeMap);
		log.info("디테일브이오" + detailVo);
		
		model.addAttribute("detailVo", detailVo);
		
		
		
		//댓글 정보 조회
		List<CommentVo> commentList = ccs.commentList(classCommNo);
		log.info("댓글리스트" + commentList);
		model.addAttribute("commentList", commentList);
		model.addAttribute("classCommNo", classCommNo);
		
		return "class_comm/detail";
	}
	
	//댓글 에이젝스

	@PostMapping("commentAjax")
	@ResponseBody
	public String comment(String content, String memberNo, String classCommNo) {
		
		//댓글 인서트
		CommentVo vo = new CommentVo();
		vo.setComContent(content);
		vo.setComWriterNo(memberNo);
		vo.setComCommentNo(classCommNo);
		
		int result = ccs.writeComment(vo);
		log.info("vo ::" + vo);
		
		//댓글 셀렉트
//		List<CommentVo> commentOne = ccs.commemtOne(vo);
		List<CommentVo> commentList = ccs.commentList(classCommNo);
		
		Gson gson = new Gson();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("commentList", commentList);
		String commentListString = gson.toJson(commentList);
		
		log.info("commentOneString ::" + commentListString);
		
		
		return commentListString;
		
	}
	
	//신고(화면) >> 아직 작동 잘 안 됨!
	@GetMapping("report")
	public String report(String blacklist, String refortTitle, Model model) {
		
		log.info("블랙리스트" + blacklist);
		log.info("글 제목" + refortTitle);

	    Gson gson = new Gson();
	    HashMap<String, Object> map = new HashMap<String, Object>();
	    
	    // key-value 형태로 맵에 저장
	    map.put("blacklist", blacklist);
	    
	    model.addAttribute("reportMap", map);
	   
//	   // 맵을 JSON Object를 바꾸고 다시 문자열로 바꿈
//	    String jsonString = gson.toJson(map);
		
		return "class_comm/report_write";
		
	}
	

	
	//신고인포
	@PostMapping("reportInfo")
	public String reportInfo(String blacklistNo, String accusor, String board, Model model) {
		
		ClassCommVo reportVo = new ClassCommVo();
		reportVo.setBlacklistNo(blacklistNo);
		reportVo.setAccusor(accusor);
		reportVo.setBoard(board);
		log.info("reportVo :: " + reportVo);
		
		int result = ccs.reportInfo(reportVo);
		
		log.info("result :: " + result);
		
		
		
		return "class_comm/report_write";
	}
	
	//신고
	@PostMapping("report")
	
	public String report(String blacklistNo, String accusor, String board, String type, HashMap<String, String> reportMap ) {
		
		ClassCommVo reportInfo = ccs.selectReportInfo();
		
		reportMap.put("blacklistNo", reportInfo.getBlacklistNo());
		reportMap.put("accusor", reportInfo.getAccusor());
		reportMap.put("board", reportInfo.getBoard());
		reportMap.put("type", type);
		reportMap.put("cate_no", "1");
		
		int reportResult = ccs.insertReport(reportMap);
		

		
		log.info("신고 :: "+type);
		log.info("reportInfo :: "+reportInfo);

		
		return "class_comm/reportSuccess";
	}
	


	
	//스터디
	@GetMapping("study")
	public String study(String orderBy, String pNo, String keyword,  Model model, Map<String, String> search) {
		
		
		//데이터 꺼내기
		if(pNo == null) {
			pNo = "1";
		}
		
		String commCateNo = "2";
		
		search.put("keyword", keyword);
		search.put("commCateNo", commCateNo);
		search.put("orderBy", orderBy);
		
		log.info("맵 : " + search);
		
		
		
		//PageVo 객체 만들기
		int listCount = ccs.selectCnt(search);
		int currentPage =  Integer.parseInt(pNo);
		int pageLimit = 5;
		int boardLimit = 3;
		
		PageVo pv = Pagination.getPageVo(listCount, currentPage, pageLimit, boardLimit);
		
		
		
		List<ClassCommVo> studyList = ccs.studyList( pv, search);
		
		List<String> studyCommentCountList = ccs.studyCommentCountList(studyList);
		
		
		log.info("studyCommentCountList리스트" + studyCommentCountList);
		log.info("pv :: " + pv);
		model.addAttribute("studyList", studyList);
		model.addAttribute("studyCommentCountList", studyCommentCountList);
		model.addAttribute("pv", pv);
		model.addAttribute("search", search);
		
		if(studyList == null) {
			return "common/error";
		}
		return "class_comm/study";
	}
	
	//자유게시판
	@GetMapping("free")
	public String free(String orderBy, Model model) {
		
		List<ClassCommVo> freeList = ccs.freeList(orderBy);
//		log.info("리스트" + studyList);
		model.addAttribute("freeList", freeList);
		if(freeList == null) {
			return "common/error";
		}
		
		return "class_comm/free";
	}
	
	


	
	//사이드바
	@GetMapping("sidebar")
	public String sidebar() {
		return "class_comm/sidebar";
	}
	
	
}
