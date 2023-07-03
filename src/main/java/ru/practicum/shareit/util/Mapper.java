package ru.practicum.shareit.util;

public interface Mapper <Domain, Dto> {
    Dto mapToDto(Domain domain);

    Domain mapToDomain(Dto dto);
}
