package com.clocktower.teamcity.api.context;

import com.clocktower.teamcity.api.context.impl.BuildTypeTreeCache;
import com.clocktower.teamcity.api.context.impl.RestService;
import com.clocktower.teamcity.api.context.impl.response.BuildTypeDto;
import com.clocktower.teamcity.api.context.impl.response.GetBuildTypesResponseDto;
import com.clocktower.teamcity.api.context.impl.response.GetProjectsResponseDto;
import com.clocktower.teamcity.api.context.impl.response.ProjectDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Context {
    private static final String ROOT_PROJECT_ID = "_Root";

    private final RestService restService;
    private BuildTypeTreeCache buildTypeTreeCache;

    public Context(String teamCityUri) {
        this(new RestService(teamCityUri));
    }

    Context(RestService restService) {
        this.restService = restService;
    }

    public String getTeamCityUri() {
        return restService.getTeamCityUri();
    }

    public Project getRootProject() {
        return buildTypeTreeCache.getProjectsById().get(ROOT_PROJECT_ID);
    }

    public Project getProject(String id) {
        return buildTypeTreeCache.getProjectsById().get(id);
    }

    public BuildType getBuildType(String id) {
        return buildTypeTreeCache.getBuildTypesById().get(id);
    }

    public void load() {
        List<Project> projects = loadProjects();
        List<BuildType> buildTypes = loadBuildTypes();
        BuildTypeTreeCache buildTypeTreeCache = new BuildTypeTreeCache(projects, buildTypes);

        Map<String, List<String>> projectIdsByParentProjectId =
                projects.stream()
                        .filter(project -> project.getParentProjectId() != null)
                        .collect(Collectors.groupingBy(Project::getParentProjectId, Collectors.mapping(Project::getId, Collectors.toList())));
        buildTypeTreeCache.getProjectsById().forEach((projectId, project) -> {
            List<String> projectIds = projectIdsByParentProjectId.get(projectId);
            project.setChildProjectIds(projectIds != null ? projectIds : new ArrayList<>());
        });

        Map<String, List<String>> buildTypeIdsByParentProjectId =
                buildTypes.stream().collect(Collectors.groupingBy(BuildType::getParentProjectId, Collectors.mapping(BuildType::getId, Collectors.toList())));
        buildTypeTreeCache.getProjectsById().forEach((projectId, project) -> {
            List<String> buildTypeIds = buildTypeIdsByParentProjectId.get(projectId);
            project.setChildBuildTypeIds(buildTypeIds != null ? buildTypeIds : new ArrayList<>());
        });

        this.buildTypeTreeCache = buildTypeTreeCache;
    }

    private List<Project> loadProjects() {
        GetProjectsResponseDto getProjectsResponseDto = restService.sendGetRequest("/projects", GetProjectsResponseDto.class);
        List<Project> projects = new ArrayList<>();
        for (ProjectDto projectDto : getProjectsResponseDto.getProject()) {
            Project project = new Project(this);
            project.setValuesFrom(projectDto);
            projects.add(project);
        }
        return projects;
    }

    private List<BuildType> loadBuildTypes() {
        GetBuildTypesResponseDto getBuildTypesResponseDto = restService.sendGetRequest("/buildTypes", GetBuildTypesResponseDto.class);
        List<BuildType> buildTypes = new ArrayList<>();
        for (BuildTypeDto buildTypeDto : getBuildTypesResponseDto.getBuildType()) {
            BuildType buildType = new BuildType(this);
            buildType.setValuesFrom(buildTypeDto);
            buildTypes.add(buildType);
        }
        return buildTypes;
    }
}
