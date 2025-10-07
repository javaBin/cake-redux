import * as cdk from 'aws-cdk-lib';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as elbv2 from 'aws-cdk-lib/aws-elasticloadbalancingv2';
import * as iam from 'aws-cdk-lib/aws-iam';
import { type Construct } from 'constructs';
import { importExistingResources } from './existing-resources';

const port = 8081;
const healthCheckPath = '/';
const priority = 15;
const desiredCount = 2;
const clusterName = 'cakeredux';

export class CakeReduxStack extends cdk.Stack {
  constructor(scope: Construct, id: string, properties?: cdk.StackProps) {
    super(scope, id, properties);

    // Import existing resources
    const { vpc, existingListener } = importExistingResources(this);


    // Create ECS Cluster
    const cluster = new ecs.Cluster(this, 'Cluster', {
      vpc,
      clusterName
    });
    // Create the Fargate service first
    const taskDefinition = new ecs.FargateTaskDefinition(this, 'TaskDef', {
      memoryLimitMiB: 512,
      cpu: 256,
    });

    // Attach the SOPS KMS policy to the task role usin arn

    taskDefinition.taskRole.addManagedPolicy(
      iam.ManagedPolicy.fromManagedPolicyArn(
        this,
        'SopsKmsPolicy',
        'arn:aws:iam::553637109631:policy/sops-kms-policy'
      )
    );

    taskDefinition.addContainer('app', {
      image: ecs.ContainerImage.fromAsset('../', { file: 'Dockerfile' }),
      portMappings: [{ containerPort: port }],
      logging: new ecs.AwsLogDriver({ streamPrefix: 'app' }),
    });

    const service = new ecs.FargateService(this, 'Service', {
      cluster,
      taskDefinition,
      desiredCount,
      assignPublicIp: true,
      circuitBreaker: { rollback: true },
    });

    // Create a target group and add it to the existing listener
    const targetGroup = new elbv2.ApplicationTargetGroup(this, 'TargetGroup', {
      vpc,
      port,
      protocol: elbv2.ApplicationProtocol.HTTP,
      targets: [service],
      healthCheck: {
        path: healthCheckPath,
      },
    });

    existingListener.addTargetGroups('AddTargetGroup', {
      targetGroups: [targetGroup],
      priority,
      conditions: [elbv2.ListenerCondition.hostHeaders(['cakeredux.javazone.no'])],
    });
  }
}
