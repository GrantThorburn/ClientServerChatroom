# ClientServerChatroom
Simple C/S chatroom, mutlithreaded with appropriate timeout/disconnect sequences. 

NOTE: I started these files using working examples provided by Retro Gamer on Stacked Overflow. The source and profile are below:

Source: https://stackoverflow.com/questions/46185206/java-send-message-to-all-clients

Profile: https://stackoverflow.com/users/4148092/retro-gamer

From the provided working example for multithreaded Client/Server, I have made the following additions:
1) Timeout sequence on the Server side
2) Disconnect sequence for timeout, and users electing to disconnect. Approriate GUI message is given, and the Server HashSets are cleared. 

What I need to work on for this file:
1) The disconnect sequence on the client side is from a NullPointerException. I need to make this exception unique either to timeout or the user electing to disconnect. The chatroom is clear when a user disconnects, but the user's GUI message should notify if the user timed out for their disconnect, rather than currently telling the user they have been disconnected without unique details.
2) Warning GUI for user as they approach the relevent timeout. This should require the client file knowing the timeout clock, counting down to a threshold. 
3) Cryptology measures (shouldn't be sending plaintext).
4) Logging features (as Retro Gamer identified). 

