package com.lawding.leavecalc.util;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

public class ParameterStoreUtils {

    private static final SsmClient ssmClient = SsmClient.builder().region(Region.AP_NORTHEAST_2)
        .build();

    public static String getSecureParameter(String name) {
        GetParameterRequest request = GetParameterRequest.builder().name(name).withDecryption(true)
            .build();
        GetParameterResponse response = ssmClient.getParameter(request);
        return response.parameter().value();
    }

}
