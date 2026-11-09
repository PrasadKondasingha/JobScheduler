package edu.ufl.ads.jobscheduler.datastructure;

public record AvlTreeStatistics(
    int records,
    int height,
    int insertions,
    int deletions,
    int leftRotations,
    int rightRotations) {}
