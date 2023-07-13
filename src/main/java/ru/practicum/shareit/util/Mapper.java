package ru.practicum.shareit.util;

public interface Mapper<D, T> {
    T mapToDto(D domain);

    D mapToDomain(T dto);
}
