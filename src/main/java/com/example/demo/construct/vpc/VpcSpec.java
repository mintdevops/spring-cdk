package com.example.demo.construct.vpc;

import com.example.demo.construct.ISpec;

import lombok.Builder;
import lombok.Data;
import software.amazon.awscdk.services.ec2.NatProvider;
import software.amazon.awscdk.services.ec2.VpcProps;

@Data
@Builder
public class VpcSpec implements VpcProps, ISpec {

    private String cidr;
    private NatProvider natProvider;
    private int publicSubnetCidrMask;
    private int privateSubnetCidrMask;
    private int isolatedSubnetCidrMask;


}
