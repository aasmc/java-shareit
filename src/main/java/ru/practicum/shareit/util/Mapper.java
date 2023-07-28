package ru.practicum.shareit.util;

public interface Mapper<D, T, R> {
    R mapToDto(D domain);

    D mapToDomain(T dto);
}
