package org.tkit.onecx.user.profile.domain.criteria;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserProfileAbstractCriteria {
    private List<String> userIds;
    private List<String> emailAddresses;
    private List<String> displayNames;
    private int pageSize;
    private int pageNumber;
}
