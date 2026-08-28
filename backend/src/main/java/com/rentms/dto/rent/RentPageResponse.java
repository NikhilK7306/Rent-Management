package com.rentms.dto.rent;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentPageResponse<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int size;
    private int number;
    private boolean first;
    private boolean last;
    private int numberOfElements;
    private boolean empty;
}