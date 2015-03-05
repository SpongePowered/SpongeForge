if [ ! -f ./SpongeAPI/gradlew ]; then
    echo "Error locating the Gradle subprojects. You most likely forgot to run 'git submodule update --init'. This usually doesn't happen to people who read the README ;)"
    # run it for them
    git submodule update --init --recursive
    # also assume they didn't run the cp
    cp scripts/pre-commit .git/hooks
fi
./SpongeAPI/gradlew $@
