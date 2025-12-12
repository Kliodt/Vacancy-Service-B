package com.vacancy.organization.utils.converters;

import java.util.Collections;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.constraints.NotNull;

@ReadingConverter
public class JsonToListLongConverter implements Converter<String, List<Long>> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<@NotNull Long> convert(@SuppressWarnings("null") String source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<Long> list = mapper.readValue(source, new TypeReference<List<Long>>() {
            });
            return list == null ? Collections.emptyList() : list;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
