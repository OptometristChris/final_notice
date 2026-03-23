package com.spring.app.js.notice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.app.auth.JwtPrincipalDTO;
import com.spring.app.js.notice.domain.NoticeDTO;
import com.spring.app.js.notice.service.NoticeService;

@Controller
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @GetMapping("/list")
    public String list(@RequestParam(value = "hotelId", defaultValue = "0") Long hotelId,
                       @RequestParam(value = "searchType", required = false) String searchType,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "curPage", defaultValue = "1") int curPage,
                       Model model) {

        List<Map<String, String>> hotelList = noticeService.getHotelList();
        model.addAttribute("hotelList", hotelList);

        int sizePerPage = 10;
        int startRow = (curPage - 1) * sizePerPage + 1;
        int endRow = startRow + sizePerPage - 1;

        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("hotelId", hotelId);
        paraMap.put("searchType", searchType);
        paraMap.put("keyword", keyword);
        paraMap.put("startRow", startRow);
        paraMap.put("endRow", endRow);

        List<NoticeDTO> topNotices = noticeService.getTopNotices(hotelId);
        List<NoticeDTO> notices = noticeService.getNoticeList(paraMap);

        int totalCount = noticeService.getTotalCount(paraMap);
        int totalPage = (int) Math.ceil((double) totalCount / sizePerPage);

        JwtPrincipalDTO principal = getLoginAdminPrincipal();
        model.addAttribute("isHq", isHq(principal));
        if (principal != null && principal.getHotelId() != null) {
            model.addAttribute("myHotelId", String.valueOf(principal.getHotelId()));
        }

        model.addAttribute("topNotices", topNotices);
        model.addAttribute("notices", notices);
        model.addAttribute("hotelId", hotelId);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("curPage", curPage);
        model.addAttribute("totalPage", totalPage);

        return "js/notice/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id,
                         @RequestParam(value = "hotelId", defaultValue = "0") Long hotelId,
                         Model model) {

        NoticeDTO notice = noticeService.getNoticeDetail(id);
        model.addAttribute("notice", notice);
        model.addAttribute("hotelId", hotelId);
        model.addAttribute("hotelList", noticeService.getHotelList());

        JwtPrincipalDTO principal = getLoginAdminPrincipal();
        model.addAttribute("isHq", isHq(principal));
        if (principal != null && principal.getHotelId() != null) {
            model.addAttribute("myHotelId", String.valueOf(principal.getHotelId()));
        }

        return "js/notice/detail";
    }

    @GetMapping("/write")
    public String showWriteForm(Model model) {
        JwtPrincipalDTO principal = getLoginAdminPrincipal();
        if (principal == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("hotelList", noticeService.getHotelList());
        model.addAttribute("isHq", isHq(principal));
        if (principal.getHotelId() != null) {
            model.addAttribute("myHotelId", String.valueOf(principal.getHotelId()));
        }

        return "js/notice/write";
    }

    @PostMapping("/write")
    public String insertNotice(NoticeDTO dto) {
        JwtPrincipalDTO principal = getLoginAdminPrincipal();
        if (principal == null) {
            return "redirect:/admin/login";
        }

        if (principal.getPrincipalNo() != null) {
            dto.setAdminNo(principal.getPrincipalNo());
        }

        if (!isHq(principal) && principal.getHotelId() != null) {
            dto.setFkHotelId(principal.getHotelId());
        }

        if (dto.getIsTop() == null) {
            dto.setIsTop("N");
        }

        noticeService.registerNotice(dto);
        return "redirect:/notice/list?hotelId=" + dto.getFkHotelId();
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        NoticeDTO notice = noticeService.getNoticeDetail(id);
        model.addAttribute("notice", notice);
        model.addAttribute("hotelId", notice.getFkHotelId());
        model.addAttribute("hotelList", noticeService.getHotelList());

        JwtPrincipalDTO principal = getLoginAdminPrincipal();
        model.addAttribute("isHq", isHq(principal));
        if (principal != null && principal.getHotelId() != null) {
            model.addAttribute("myHotelId", String.valueOf(principal.getHotelId()));
        }

        return "js/notice/edit";
    }

    @PostMapping("/edit")
    public String updateNotice(NoticeDTO dto, RedirectAttributes rttr) {
        JwtPrincipalDTO principal = getLoginAdminPrincipal();
        if (principal == null) {
            return "redirect:/admin/login";
        }

        if (!isHq(principal) && principal.getHotelId() != null) {
            dto.setFkHotelId(principal.getHotelId());
        }

        int result = noticeService.updateNotice(dto);
        rttr.addFlashAttribute("message", result > 0 ? "수정 완료." : "수정 실패.");
        return "redirect:/notice/detail/" + dto.getNoticeId() + "?hotelId=" + dto.getFkHotelId();
    }

    @PostMapping("/delete")
    public String deleteNotice(@RequestParam("noticeId") Long noticeId, RedirectAttributes rttr) {
        NoticeDTO notice = noticeService.getNoticeDetail(noticeId);
        Long hotelId = (notice != null) ? notice.getFkHotelId() : 0L;
        int result = noticeService.deleteNotice(noticeId);
        rttr.addFlashAttribute("message", result > 0 ? "성공적으로 삭제되었습니다." : "삭제 실패.");
        return "redirect:/notice/list?hotelId=" + hotelId;
    }

    private JwtPrincipalDTO getLoginAdminPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principalObj = auth.getPrincipal();
        if (!(principalObj instanceof JwtPrincipalDTO principal)) return null;
        if (principal.getRoles() == null) return null;
        boolean isAdmin = principal.getRoles().stream().anyMatch(role -> "ROLE_ADMIN_HQ".equals(role) || "ROLE_ADMIN_BRANCH".equals(role));
        return isAdmin ? principal : null;
    }

    private boolean isHq(JwtPrincipalDTO principal) {
        if (principal == null) return false;
        if ("HQ".equalsIgnoreCase(principal.getAdminType())) return true;
        return principal.getRoles() != null && principal.getRoles().stream().anyMatch("ROLE_ADMIN_HQ"::equals);
    }
}
