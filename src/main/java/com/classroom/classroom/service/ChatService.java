package com.classroom.classroom.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.classroom.entity.ChatMessage;
import com.classroom.classroom.mapper.ChatMessageMapper;
import com.classroom.classroom.vo.ChatMessageVO;
import com.classroom.auth.entity.User;
import com.classroom.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天消息服务
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageMapper messageMapper;
    private final UserMapper userMapper;

    /**
     * 保存消息
     */
    public ChatMessage saveMessage(Long classroomId, String content, String msgType) {
        long userId = StpUtil.getLoginIdAsLong();

        ChatMessage message = new ChatMessage();
        message.setClassroomId(classroomId);
        message.setSenderId(userId);
        message.setContent(content);
        message.setMsgType(msgType != null ? msgType : "text");

        messageMapper.insert(message);
        return message;
    }

    /**
     * 获取历史消息
     */
    public Page<ChatMessageVO> getMessages(Long classroomId, int page, int size) {
        Page<ChatMessage> pageResult = messageMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getClassroomId, classroomId)
                        .orderByDesc(ChatMessage::getCreateTime));

        // 获取发送者信息
        List<Long> senderIds = pageResult.getRecords().stream()
                .map(ChatMessage::getSenderId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = senderIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(senderIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        Page<ChatMessageVO> voPage = new Page<>(page, size, pageResult.getTotal());
        voPage.setRecords(pageResult.getRecords().stream()
                .map(m -> toVO(m, userMap.get(m.getSenderId())))
                .collect(Collectors.toList()));

        return voPage;
    }

    private ChatMessageVO toVO(ChatMessage message, User sender) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setClassroomId(message.getClassroomId());
        vo.setSenderId(message.getSenderId());
        vo.setContent(message.getContent());
        vo.setMsgType(message.getMsgType());
        vo.setCreateTime(message.getCreateTime());

        if (sender != null) {
            vo.setSenderName(sender.getNickname());
            vo.setSenderAvatar(sender.getAvatar());
        }

        return vo;
    }
}
