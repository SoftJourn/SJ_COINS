package com.softjourn.coin.server.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.softjourn.coin.server.util.SortJsonDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageRequestImpl {

  private int size;
  private int page;

  @JsonDeserialize(using = SortJsonDeserializer.class)
  private Sort sort;

  public Pageable toPageable() {
    if (sort == null) {
      return PageRequest.of(page, size);
    } else {
      return PageRequest.of(page, size, sort);
    }
  }
}
