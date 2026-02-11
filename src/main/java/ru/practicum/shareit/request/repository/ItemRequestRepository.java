package ru.practicum.shareit.request.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ItemRequestRepository {

    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private final Map<Long, List<ItemRequest>> userRequests = new HashMap<>();
    private Long idCounter = 1L;

    public ItemRequest save(ItemRequest request) {
        if (request.getId() == null) {
            request.setId(idCounter++);
        }

        requests.put(request.getId(), request);

        Long requestorId = request.getRequestor().getId();
        userRequests.computeIfAbsent(requestorId, k -> new ArrayList<>()).add(request);

        return request;
    }

    public Optional<ItemRequest> findById(Long id) {
        return Optional.ofNullable(requests.get(id));
    }

    public List<ItemRequest> findAllByRequestorId(Long requestorId) {
        return userRequests.getOrDefault(requestorId, Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .collect(Collectors.toList());
    }

    public List<ItemRequest> findAllExceptRequestor(Long requestorId) {
        return requests.values().stream()
                .filter(request -> !request.getRequestor().getId().equals(requestorId))
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .collect(Collectors.toList());
    }

    public List<ItemRequest> findAll() {
        return new ArrayList<>(requests.values());
    }

    public void deleteById(Long id) {
        ItemRequest request = requests.remove(id);
        if (request != null) {
            Long requestorId = request.getRequestor().getId();
            List<ItemRequest> userRequestsList = userRequests.get(requestorId);
            if (userRequestsList != null) {
                userRequestsList.remove(request);
            }
        }
    }

    public boolean existsById(Long id) {
        return requests.containsKey(id);
    }
}