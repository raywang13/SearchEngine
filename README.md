# Search Engine Project

## Description

This is a Search Engine that utilizes a multithreaded web crawler. The Front End design is done using HTML/CSS and the Back End is done using SQL. 

The program crawls through a given link and parses out the HTML. The crawler will also look at inner sub-links and store all the text into a data structure that keeps track of each word's position, frequency, and what page it was found on.

Then it will execute a partial search based on a query input, returning results in order from most to least relevant. Relevancy is determined base on the position and frequency of a word.

Finally, the search result will be displayed using HTML back to the user.

There is also an addition feature allowing users to create personal accounts on the search engine. The project is still a work in progress as I plan to add features such as private searching, history tracking, and bookmarks.
