package com.walrex.gateway.gateway.config;

import com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.entity.ModulesUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PathTransformer {

    public String stripPrefix(String requestPath, ModulesUrl module) {
        log.debug("stripPrefix - Input: '{}', stripCount: {}", requestPath, module.getStripPrefixCount());

        if (module.getStripPrefixCount() != null && module.getStripPrefixCount() > 0) {
            String[] segments = requestPath.split("/");
            int segmentsToSkip = module.getStripPrefixCount();
            StringBuilder strippedPath = new StringBuilder();
            for (int i = segmentsToSkip + 1; i < segments.length; i++) {
                if (!segments[i].isEmpty()) {
                    strippedPath.append("/").append(segments[i]);
                }
            }
            String result = strippedPath.length() > 0 ? strippedPath.toString() : "/";
            log.debug("stripPrefix - Output: '{}'", result);
            return result;
        }
        return requestPath;
    }
}
