package com.campus.infrastructure.persistence.notice;

import com.campus.domain.notice.entity.Notice;
import com.campus.domain.notice.entity.NoticeAttachment;
import com.campus.domain.notice.entity.NoticeRead;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NoticeConverter {

    // ===== Notice =====

    public Notice toNotice(NoticePO po) {
        if (po == null) return null;
        Notice notice = new Notice();
        BeanUtils.copyProperties(po, notice);
        return notice;
    }

    public NoticePO toNoticePO(Notice notice) {
        if (notice == null) return null;
        NoticePO po = new NoticePO();
        BeanUtils.copyProperties(notice, po);
        return po;
    }

    public List<Notice> toNoticeList(List<NoticePO> poList) {
        if (poList == null || poList.isEmpty()) return new ArrayList<>();
        return poList.stream().map(this::toNotice).collect(Collectors.toList());
    }

    // ===== NoticeAttachment =====

    public NoticeAttachment toNoticeAttachment(NoticeAttachmentPO po) {
        if (po == null) return null;
        NoticeAttachment att = new NoticeAttachment();
        BeanUtils.copyProperties(po, att);
        return att;
    }

    public List<NoticeAttachment> toNoticeAttachmentList(List<NoticeAttachmentPO> poList) {
        if (poList == null || poList.isEmpty()) return new ArrayList<>();
        return poList.stream().map(this::toNoticeAttachment).collect(Collectors.toList());
    }

    public NoticeAttachmentPO toNoticeAttachmentPO(NoticeAttachment att) {
        if (att == null) return null;
        NoticeAttachmentPO po = new NoticeAttachmentPO();
        BeanUtils.copyProperties(att, po);
        return po;
    }

    // ===== NoticeRead =====

    public NoticeRead toNoticeRead(NoticeReadPO po) {
        if (po == null) return null;
        NoticeRead record = new NoticeRead();
        BeanUtils.copyProperties(po, record);
        return record;
    }

    public NoticeReadPO toNoticeReadPO(NoticeRead record) {
        if (record == null) return null;
        NoticeReadPO po = new NoticeReadPO();
        BeanUtils.copyProperties(record, po);
        return po;
    }
}
