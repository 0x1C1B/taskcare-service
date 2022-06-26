package de.x1c1b.taskcare.service.presentation.rest.v1;

import de.x1c1b.taskcare.service.core.board.application.BoardService;
import de.x1c1b.taskcare.service.core.board.application.command.AddBoardMemberByIdCommand;
import de.x1c1b.taskcare.service.core.board.application.command.ChangeBoardMemberRoleByIdCommand;
import de.x1c1b.taskcare.service.core.board.application.command.RemoveBoardMemberByIdCommand;
import de.x1c1b.taskcare.service.core.board.application.query.FindBoardByIdQuery;
import de.x1c1b.taskcare.service.core.board.domain.Board;
import de.x1c1b.taskcare.service.core.common.application.EntityNotFoundException;
import de.x1c1b.taskcare.service.presentation.rest.v1.dto.CreateBoardMemberDTO;
import de.x1c1b.taskcare.service.presentation.rest.v1.dto.MemberDTO;
import de.x1c1b.taskcare.service.presentation.rest.v1.dto.UpdateBoardMemberDTO;
import de.x1c1b.taskcare.service.presentation.rest.v1.dto.mapper.MemberDTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@Validated
public class MemberController {

    private final BoardService boardService;
    private final MemberDTOMapper memberDTOMapper;

    @Autowired
    public MemberController(BoardService boardService, MemberDTOMapper memberDTOMapper) {
        this.boardService = boardService;
        this.memberDTOMapper = memberDTOMapper;
    }

    @GetMapping("/boards/{id}/members")
    List<MemberDTO> findAll(@PathVariable("id") UUID id) {
        FindBoardByIdQuery query = new FindBoardByIdQuery(id);
        Board result = boardService.query(query);
        return memberDTOMapper.mapToDTO(result.getMembers());
    }

    @GetMapping("/boards/{id}/members/{username}")
    MemberDTO findByUsername(@PathVariable("id") UUID id, @PathVariable("username") String username) {
        FindBoardByIdQuery query = new FindBoardByIdQuery(id);
        Board result = boardService.query(query);
        return memberDTOMapper.mapToDTO(result.getMembers().stream()
                .filter(member -> member.getUsername().equals(username)).findFirst().orElseThrow(EntityNotFoundException::new));
    }

    @PatchMapping("/boards/{id}/members/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateByUsername(@PathVariable("id") UUID id, @PathVariable("username") String username, @RequestBody UpdateBoardMemberDTO dto) {
        ChangeBoardMemberRoleByIdCommand command = memberDTOMapper.mapToCommand(dto, id, username);
        boardService.execute(command);
    }

    @PostMapping("/boards/{id}/members")
    @ResponseStatus(HttpStatus.CREATED)
    void addMember(@PathVariable("id") UUID id, @RequestBody CreateBoardMemberDTO dto) {
        AddBoardMemberByIdCommand command = memberDTOMapper.mapToCommand(dto, id);
        boardService.execute(command);
    }

    @DeleteMapping("/boards/{id}/members/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeMember(@PathVariable("id") UUID id, @PathVariable("username") String username) {
        RemoveBoardMemberByIdCommand command = new RemoveBoardMemberByIdCommand(id, username);
        boardService.execute(command);
    }
}
