package com.vacancy.organization.utils.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.util.List;

@WritingConverter
public class ListLongToJsonConverter implements Converter<List<Long>, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convert(@SuppressWarnings("null") List<Long> source) {
        if (source == null || source.isEmpty()) {
            return "[]";
        }
        try {
            return mapper.writeValueAsString(source);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert List to JSON: " + source, e);
        }
    }
}
