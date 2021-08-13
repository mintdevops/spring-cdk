package com.example.demo.core;

/**
 * Simple enum to determine the type of lookup to perform for resources outside of *this* CDK application.
 *
 * Synth-time lookups will bake values in to the CloudFormation templates during `cdk synth` whereas deploy-time
 * lookups will resolve them during `cdk-deploy`. The user should be careful about choosing the correct strategy as it
 * can effect the behaviour of the infrastructure.
 *
 * For example, looking up an AMI ID at deploy-time will not provide you with any guarantees that the underlying
 * image has not changed. If you are running an AutoScalingGroup you could unknowingly release a new, event untested
 * version. In this case a synth-time lookup makes more sense.
 */
public enum LookupType {
    SYNTH,
    DEPLOY
}
