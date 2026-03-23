package com.spring.app.js.notice.model;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.spring.app.js.notice.domain.NoticeDTO;

@Mapper
public interface NoticeDAO {
    List<NoticeDTO> selectNoticeList(@Param("hotelId") Long hotelId);
    NoticeDTO selectNoticeDetail(@Param("noticeId") Long noticeId);
    int insertNotice(NoticeDTO dto);
    int updateNotice(NoticeDTO dto);
    int deleteNotice(@Param("noticeId") Long noticeId);
    int getTotalCount(Map<String, Object> paraMap);
    List<NoticeDTO> selectNoticeListWithSearch(Map<String, Object> paraMap);
    List<NoticeDTO> selectTopNotices(@Param("hotelId") Long hotelId);
    List<Map<String, String>> getHotelList();
}
