# Release notes `valdiation` library

## 0.0.2 (in progress)

- When validating via `Validator`, for each validation of an object the following `Events` are sent:
  - With payload `ValidationStartedPayload`, immediately before the first rule for the object is executed.
  - With payload `ValidationFinishedPayload`, immediately after the last rulr for the object is executed.

## 0.0.1

Initial version