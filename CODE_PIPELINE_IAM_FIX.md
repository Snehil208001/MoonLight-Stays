# Fix: CodePipeline "elasticbeanstalk:CreateApplicationVersion" Permission Error

## Problem
Deploy stage fails with: **"The provided role does not have the `elasticbeanstalk:CreateApplicationVersion` permission"**

## Solution
Add Elastic Beanstalk permissions to the **CodePipeline service role**.

---

## Steps

### 1. Find the CodePipeline service role
1. Go to **AWS Console → IAM → Roles**
2. Search for `codepipeline` or `airbnb-backend`
3. Click the role (e.g. `codepipeline-airbnb-backend-role` or `AWSCodePipelineServiceRole-ap-south-1-airbnb-backend-pipeline`)

### 2. Add inline policy
1. Click **Add permissions** → **Create inline policy**
2. Go to the **JSON** tab
3. Paste this policy (full EB deploy permissions):

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "AllowElasticBeanstalkDeploy",
            "Effect": "Allow",
            "Action": [
                "elasticbeanstalk:*",
                "ec2:DescribeInstanceStatus",
                "ec2:DescribeInstances",
                "elasticloadbalancing:DescribeLoadBalancers",
                "autoscaling:DescribeAutoScalingGroups",
                "s3:GetObject",
                "s3:PutObject",
                "s3:DeleteObject",
                "s3:ListBucket"
            ],
            "Resource": "*"
        }
    ]
}
```

4. Click **Next**
5. Policy name: `CodePipeline-ElasticBeanstalk-Deploy`
6. Click **Create policy**

### 3. Re-run the pipeline
1. Go back to **CodePipeline** → **airbnb-backend-pipeline**
2. Click **Release change**

---

## Alternative: Use AWS managed policy
If the above doesn't work, attach the managed policy **`AWSElasticBeanstalkFullAccess`** to the CodePipeline role (broader permissions, but simpler).
