package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static ru.practicum.shareit.testutil.TestDataProvider.*;
import static ru.practicum.shareit.testutil.TestConstants.*;


@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentMapper mapper;
    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void getItemIdToComments_whenNoComments_returnsEmptyMap() {
        Mockito
                .when(commentRepository.findAllByItem_IdIn(Set.of(1L)))
                .thenReturn(Collections.emptyList());

        Map<Long, List<CommentResponse>> itemIdToComments = commentService.getItemIdToComments(Set.of(1L));
        assertThat(itemIdToComments).isEmpty();
    }

    @Test
    void getItemIdToComments_returnsCorrectMap() {
        List<Comment> comments = new ArrayList<>();
        for (long i = 1; i <= 5; i++) {
            comments.add(getComment(i, i));
        }
        Mockito
                .when(commentRepository.findAllByItem_IdIn(Set.of(1L)))
                .thenReturn(comments);
        List<CommentResponse> dtos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            dtos.add(fromDomain(comments.get(i)));
        }
        for (int i = 0; i < 5; i++) {
            Mockito
                    .when(mapper.mapToDto(comments.get(i)))
                    .thenReturn(dtos.get(i));
        }

        Map<Long, List<CommentResponse>> itemIdToComments = commentService.getItemIdToComments(Set.of(1L));
        assertThat(itemIdToComments.size()).isEqualTo(comments.size());
        for (long i = 1; i <= 5; i++) {
            List<CommentResponse> responses = itemIdToComments.get(i);
            assertThat(responses.size()).isEqualTo(1);
            CommentResponse response = responses.get(0);
            assertThat(response).isEqualTo(dtos.get((int) i - 1));
        }
    }

    @Test
    void getCommentsOfItem_whenHasComments_returnsThem() {
        List<Comment> comments = new ArrayList<>();
        for (long i = 1; i <= 5; i++) {
            comments.add(getComment(i, i));
        }
        Mockito
                .when(commentRepository.findAllByItem_Id(1L))
                .thenReturn(comments);
        List<CommentResponse> dtos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            dtos.add(fromDomain(comments.get(i)));
        }
        for (int i = 0; i < 5; i++) {
            Mockito
                    .when(mapper.mapToDto(comments.get(i)))
                    .thenReturn(dtos.get(i));
        }

        List<CommentResponse> commentsOfItem = commentService.getCommentsOfItem(1L);
        assertThat(commentsOfItem).isEqualTo(dtos);
    }

    @Test
    void getCommentsOfItem_whenNoComments_returnsEmptyList() {
        Mockito
                .when(commentRepository.findAllByItem_Id(1L))
                .thenReturn(Collections.emptyList());
        List<CommentResponse> commentsOfItem = commentService.getCommentsOfItem(1L);
        assertThat(commentsOfItem).isEmpty();
    }

    @Test
    void saveComment_savesCommentSuccessfully() {
        Item item = getAvailableItemWithoutBookings();
        User user = getMockUser(OWNER_ID);
        CommentRequest dto = getCommentRequest(user.getId(), item.getId());
        Comment comment = getComment(dto, item, user);
        Comment saved = comment.setId(1L);
        CommentResponse expected = fromDomain(saved);
        Mockito
                .when(mapper.mapToDomain(dto)).thenReturn(comment);
        Mockito
                .when(commentRepository.save(comment)).thenReturn(saved);
        Mockito
                .when(mapper.mapToDto(saved)).thenReturn(expected);

        CommentResponse actual = commentService.saveComment(dto);

        assertThat(actual).isEqualTo(expected);
    }



}