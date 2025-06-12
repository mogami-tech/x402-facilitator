# Development ==========================================================================================================
run_install:
    mvn install -DskipTests

run_tests:
    mvn clean install

run_application:
    mvn spring-boot:run -Dspring-boot.run.profiles=development

# Docker ===============================================================================================================
build_docker_image:
    mvn spring-boot:build-image -P release

# Release ==============================================================================================================
run_deploy_snapshot:
    mvn -B -Prelease -DskipTests clean deploy

start_release:
    git remote set-url origin git@github.com:mogami-tech/x402-facilitator .git
    git checkout development
    git pull
    git status
    mvn gitflow:release-start

finish_release:
    mvn gitflow:release-finish -DskipTests

run_deploy_release:
    mvn -B -Prelease -DskipTests clean deploy
