package io.elasticore.springboot3.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Interface for objects that support pagination functionality.
 * 
 * This interface extends SortableObject to provide pagination capabilities
 * with page number and page size configuration for Spring Data queries.
 */
public interface PageableObject extends SortableObject {

    /**
     * Creates a Pageable instance using the current pagination and sorting configuration.
     * 
     * @return Pageable instance with page number, page size, and optional sorting
     */
    default Pageable getPageable() {
        Sort sort = getSort();
        if(sort!=null)
            return PageRequest.of(getPageNumber(), getPageSize(), getSort());
        else
            return PageRequest.of(getPageNumber(), getPageSize());
    }

    /**
     * Gets the page number (zero-based).
     * 
     * @return the page number starting from 0
     */
    int getPageNumber();

    /**
     * Gets the number of items per page.
     * 
     * @return the page size (number of items per page)
     */
    int getPageSize();


}
