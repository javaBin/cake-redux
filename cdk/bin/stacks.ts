
import * as cdk from 'aws-cdk-lib';
import {CakeReduxStack } from '../lib/cakeredux-stack.js';

const app = new cdk.App();
new CakeReduxStack(app, 'CakeReduxStack', {
  env: { account: '553637109631', region: 'eu-central-1' },
});


