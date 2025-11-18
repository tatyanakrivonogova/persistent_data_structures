BUILD_TOOL          := mvn
DOC_BUILD_TOOL		:= mvn

SOURCE_PATH         := .

BUILD_ARGS          := -f $(SOURCE_PATH) clean package
GEN_DOC_ARGS        := javadoc:javadoc
TEST_ARGS			:= surefire:test

test:
	$(BUILD_TOOL) $(TEST_ARGS)

build:
	$(BUILD_TOOL) $(BUILD_ARGS)

generate-doc:
	$(DOC_BUILD_TOOL) $(GEN_DOC_ARGS)