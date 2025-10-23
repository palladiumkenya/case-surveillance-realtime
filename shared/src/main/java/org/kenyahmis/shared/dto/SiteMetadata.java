package org.kenyahmis.shared.dto;

import java.util.Set;

public record SiteMetadata(
        Set<String> mflCodes,
        String emrVersion
) {
}
