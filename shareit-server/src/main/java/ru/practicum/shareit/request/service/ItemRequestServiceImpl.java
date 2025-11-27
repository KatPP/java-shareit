package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ItemRequestDto create(Long requestorId, String description) {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Описание запроса не может быть пустым");
        }
        var requestor = userService.getUserById(requestorId);
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setCreated(LocalDateTime.now());
        request.setRequestor(requestor);
        return ItemRequestMapper.toItemRequestDto(requestRepository.save(request), itemRepository);
    }

    @Override
    public List<ItemRequestDto> getOwn(Long requestorId) {
        userService.getUserById(requestorId);
        return requestRepository.findByRequestor_IdOrderByIdDesc(requestorId)
                .stream()
                .map(r -> ItemRequestMapper.toItemRequestDto(r, itemRepository))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAll(Long requestorId, int from, int size) {
        userService.getUserById(requestorId);
        PageRequest page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));
        return requestRepository.findByRequestor_IdNotOrderByIdDesc(requestorId, page)
                .stream()
                .map(r -> ItemRequestMapper.toItemRequestDto(r, itemRepository))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getById(Long requestId, Long userId) {
        if (userId != null) {
            userService.getUserById(userId);
        }
        var request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ru.practicum.shareit.exception.NotFoundException("Запрос не найден"));
        return ItemRequestMapper.toItemRequestDto(request, itemRepository);
    }
}