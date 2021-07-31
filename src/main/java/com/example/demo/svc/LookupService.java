package com.example.demo.svc;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class LookupService {

    // TODO: These values come from SSM (need to know external stack name only)
    public String getVpcId() {
        return "vpc-1235";
    }

    public List<String> getAvailabilityZones() {
        return Arrays.asList("eu-west-1a");
    }

    public String pickSubnet() {
        return "subnet-12345";
    }
}
