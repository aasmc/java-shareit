package ru.practicum.shareit.util;

public interface Mapper <Domain, Dto, Id> {
    Dto mapToDto(Domain domain);

    Domain mapToDomain(Dto dto, Id domainId);
}
