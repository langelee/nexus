
Scenario: saving a new task

Given a new unsaved task is being edited
When Save button is clicked
Then validate the entered data
And save the entered data to server
