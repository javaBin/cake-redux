import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import { Construct } from 'constructs';
export interface ExistingResources {
  vpc: ec2.IVpc;
  existingListener: elbv2.IApplicationListener;
}

export function importExistingResources(scope: Construct): ExistingResources {
  // Import existing VPC
  const vpc = ec2.Vpc.fromLookup(scope, 'ImportedVPC', {
    isDefault: true, // Assuming you're using the default VPC
  });

  // Import the existing ALB
  const existingAlb =
    elbv2.ApplicationLoadBalancer.fromApplicationLoadBalancerAttributes(
      scope,
      'ExistingALB',
      {
        loadBalancerArn:
          'arn:aws:elasticloadbalancing:eu-central-1:553637109631:loadbalancer/app/Default-ALB/1c9b1a5fd7bded5e',
        loadBalancerCanonicalHostedZoneId: 'Z215JYRZR1TBD5',
        loadBalancerDnsName:
          'Default-ALB-1235825687.eu-central-1.elb.amazonaws.com',
        securityGroupId: 'sg-06d60bddabf369fb2',
        vpc,
      }
    );

  // Import existing listener
  const existingListener =
    elbv2.ApplicationListener.fromApplicationListenerAttributes(
      scope,
      'ExistingListener',
      {
        listenerArn:
          'arn:aws:elasticloadbalancing:eu-central-1:553637109631:listener/app/Default-ALB/1c9b1a5fd7bded5e/b211e068a4b86a7b', // Replace with your listener ARN
        securityGroup: existingAlb.connections.securityGroups[0],
      }
    );


  return {
    vpc,
    existingListener,
  };
}
