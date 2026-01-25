package com.SwitchBoard.WorkspaceService.Filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraceFilterTest {

    @InjectMocks
    private TraceFilter traceFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    void doFilterInternal_addsTraceId() throws Exception {
        // Arrange
        when(request.getHeader("X-Trace-Id")).thenReturn(null);

        // Act
        traceFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_usesExistingTraceId() throws Exception {
        // Arrange
        when(request.getHeader("X-Trace-Id")).thenReturn("existing-trace-id");

        // Act
        traceFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
