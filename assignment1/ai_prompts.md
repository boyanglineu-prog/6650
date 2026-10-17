# AI Prompts

Prompts I sent to the AI assistant (Claude Code) while working on this assignment, in order. Pasted logs, code, and instructions are summarized in [brackets].

1. what is tomcat
2. where to code my java? how to "upload" onto tomcat?
3. slow down, what is maven?
4. like kubecuilder?
5. maven vs gradle,which should I pick for class project. also, which one is the industry standard?
6. ok maven it is. what is servelet? war?
7. like fastAPI?
8. so servlet must be used with tomcat?
9. The container can be embedded... tell me more, conpare this with war approach
10. I will use WAR in for this proj
11. JDK is the language tool. I use Maven as a framework to code my own logic. Then convert it into a WAR. next I copy the WAR file to my server, which is a EC2 instance, with tomcat installed already. After copy to specific dir, tomcat will run the WAR (with my logic inside). correct me as needed. what is 5. loop?
12. what is scp, in the copy file phase
13. give that tomcat is aleady installed, next is to code java locally
14. Don't do it for me in this proj, tell me what to do when I ask / servelet is a java lib?
15. jarkata? the capital city of indonnisai?
16. anyway, let me creat a new proj in ItelliJ
17. what is WebSocket API?
18. what is apache maven
19. [pasted: java Counter 1 ClassNotFoundException error] how to cmopile?
20. can just use java Counter 1 / java Counter 100
21. I do have package line, because it belongs to certain package
22. so I don't need type the package name, but I must type .java if so, is the file compiled or not? not to .class yet?
23. [pasted: Thread/latch loop snippet] syntax
24. what is latch.ddountDown?
25. like sema?
26. private static final Object lock = new Object(); why/
27. just a meanningless target? the only purpose is it existing?
28. it act as the lock itself
29. Thread()?
30. Thread().start() calles run() mathod of the runable?
31. if the runnable is a lambda defined inside Thread(), no need to define a run() for the lambda, rihgt?
32. if i declare mutiple Object lock1, lock2, .. , then I can syncronized() on mutiple locks? in short
33. Vector\<> vs ArrayList\<> in terms of synchronizarition overhead, in short
34. similarly, HashMap, HashTable, ConconcurrentHashMap?
35. how to reset latch countdown number
36. [pasted: benchmark output] MultiThreadHashTable time: 5 ms / HashMap 361 ms / ConcurrentHashMap 153 ms why? concurenthashtmap suppose to be the fastest?
37. go check my source code in java/main/CollectionPerformance.java
38. [pasted: further benchmark timings, several times] now?
39. what noe
40. check my code now
41. [pasted: "Add Web Application Support" instructions] how
42. there is no 'add framework support'
43. no, I am using the formal edition, with educaiton sucscription
44. JRE Java 11?
45. Error: 'Java11' is not a valid JRE home
46. [pasted: Step 3.1 instructions] how to select Java 11? I just see 17 22 24 ms-17, etc, no Java 11
47. [pasted: Step 3.2 instructions] I am in deployment tab, I can see '+', but no Artifact, just exxternal source
48. let me do it all over again / wiat
49. [pasted: Step 2.2 instructions] I can't see 'Add Framework Support'
50. B, i need maven
51. I don't have the web/ at all
52. the result should be like this [pasted: project layout]
53. I created those dir and files mysefl
54. [pasted: full guide, Part 2] I have done all these, now I am gooing to do step 3
55. [pasted: mvn error, run from the wrong directory]
56. [pasted: successful mvn build log]
57. http://localhost:8080/ski_servlet/ what should I see on this page?
58. not really, I saw 404 Not FOund
59. [pasted: Tomcat stack trace] still 404 intelliJ says
60. [pasted: further Tomcat logs]
61. still 404 in my configuration, it is like: URL http://localhost:8080/ application context: /ski_servlet
62. no the root also 404
63. [pasted: unzip -l output of the WAR]
64. this part already in pom
65. [pasted: Tomcat log]
66. yes, ski-sevlet shows, but the homepage is 404
67. so WAR is simply a .war compressed file? ...
68. how is this .war generated from my source code?
69. mvn clean package is like a 'build'?
70. java: package com.google.gson does not exist
71. [pasted: Postman curl 500 error] but I got this
72. import javax.servlet.http.HttpServlet; I have this in my SkierServlet
73. public class SkierServlet extends HttpServlet { have this
74. [pasted: chown tomcat:tomcat error on EC2]
75. [pasted: ps aux output on EC2]
76. [pasted: ls -l /opt/tomcat9/webapps output]
77. [pasted: Postman Step 4.3 instructions] I can't find the 'tests' tab in postman
78. ws://localhost:8080/ski-servlet/echo what does this command mean?
79. can it be reached in browser?
80. mvn clean compile war:war vs mvn clean package
81. [pasted: EchoClient compile errors]
82. [pasted: EchoClient guide, Steps 4-5] per the instruction, I think the EchoClient.java is in the ski-servlet folder
83. I click run in the EchoClient file, get this
84. 1. Run → Edit Configurations → select the EchoClient application config. how? in detail
85. [pasted: pom.xml] this is my current pom.xml
86. like this?
87. instead of delete, how to commentout?
88. will such change affect my ski-servlet when I deploy to EC2 tomcat?
89. [pasted: EchoClient success output]
90. what is the function of pom.xml?
91. in /target/ are the files built by maven and will be copied to server?
92. what is web.xml, index.jsp?
93. web.xml is like to 'route' the addresses to different classes?
94. 1. Your SkierServlet opens and closes a connection per request; the echo client holds one connection open for many messages. When does each model win? ... am I right?
95. 2. What does the server have to keep in memory for every open WebSocket connection ...?
96. websocket is stateful so that can push to user message wheneven there is update in database. I was wondering, in industry, how to handle this scenario without holding millions of alive session?
97. so the connection is not always open, but the session info is always stored.
98. tell me more about the work flow of subscription, kafka, sqs, websocket, connection, etc
99. what does it mean by 'keep the socket alive', 'keep the session alive', 'keep the connecion alive'?
100. tell me more, http vs websocket
101. http and websocket are both built on TCP?
102. in terms of subscription, why use websocket, why not let the user app send periodic get http request to server?
103. so polling by HTTP is like a series of independent HTTP requests. while pushing needs a ready pool of 'whom do I send to',
104. what role does websocket play in the mechanism of push?
105. in order to get youtube subscriptio push, I need to create a websocket for youtube's use?
106. put away webhook aside first. what if I close the app, or turn off the device, what will happen ot the websocket?
107. so as long as the websocket is alive, ... the app will auto send a requet to server carying 'last received message' ...?
108. skierservlet extends HTTPsefvlet, what it doGet(), doPut() in it?
109. tomcat receive the HTTP request, who will decide calling which function?
110. in my skiersevlet.java,there is already validaiton on url and liftride, how to return 400 on failure
111. to be specific, in line 56, there is already return 400 failure, just not using a number 400?
112. REST Endpoint: GET /health A simple GET returning canned server status. what dies it mean?
113. [pasted: health branch code] is it correct?
114. no I think the health path should similar to [stats branch]
115. I believe it is skier/health
116. is the variable json a string?
117. so the json string is like '{sdfsfa}'?
118. [pasted: syntheticLeaderboard method] syntax?
119. in simple words, below is what I want: ... how to return this in private static String syntheticLeaderboard(String key){} method?
120. is it the typical operation for generating a canned json format string?
121. for now I just want a hardcoded one
122. which file direct /skier-servlet to my SkierServlet class?
123. [pasted: isUrlValid code and a curl returning 200] but why ... skier vs resorts?
124. no I have changed
125. now my /ski-servlet/ becomes ... invalid url
126. [pasted: web.xml with /resorts/*, /health, /stats] so for now the three url pattern will be directed to my SkierServlet class? in short
127. yes, that is my question, how to make stats and health work?
128. so getServletPath is geting the /stats, /health part, while getPahtInfo is getting the later parts?
129. to clarify, pom.xml is about maven dependency. in short
130. web.xml configures the website routing
131. how to route to /leaderboard? as a websocket
132. so websocket doesn't need redirection in web.xml
133. I have a echo class, @ServerEndpoint("/echo") but [curl http://localhost:8080/ski-servlet/echo returns HTTP Status 404 – Not Found]
134. [pasted: ExecutorService snippet] explain
135. what is a Future
136. [pasted: Runnable submit snippet] this part alone, will I get 1 or 5 output in terminal?
137. so submit is like submit a task to the thread pool to excute, the 5 threads are there standing by, if I have 5 tasks(can be different?), I will need to submit 5 times, in turn, I will have 5 result from future.get()
138. Create 32 threads at startup / Each thread sends 1,000 swipe POSTs, then terminates / what is the code look like? use java.net.http.HttpClient
139. use threadpool executor.
140. pool.awaitTerination?
141. [pasted: compile error, package javax.servlet.http does not exist] ??
142. I am using a threadpool executor the ingest the request, I also need a messageGenerator, as a dedicated thread, to generate message. I will write a method accodingly, how to create such dedicatedd thhread?
143. what if I write a seperate method?
144. randint() 1~10?
145. now I just need a single thread randint
146. I need url and header(?what is the right term), do I need two generator ?
147. [pasted: SwipeMessage class] I made a seperate class for generating the random swipemessage
148. public vs public static
149. blocking queue cannnot be instantiated? how to declare and use?
150. [pasted: main with this.generateRandomSwipeMessage()] why calling that method "make main not static"
151. but the generate method is usign a blocking queue, which is a private filed of the cloas
152. SwipeMessage message = SwipeMessage.randomMessage(); if using blockingqueuw, how to moidfy this line
153. does queue.take need a seperate try-catch?
154. intelliJ hint me: surround with try/catch
155. check my swipeclient.java, tell me if any bug
156. check again
157. again
158. again
159. take a look at my subscribeclient.java, I want to [pasted: requirements: open a pool of persistent WebSocket connections to /leaderboard/{resortID}; keep them connected for the ingest run; record receipt time and serverTimestamp per message; handle connection drops and reconnect gracefully]
160. check again, and edit
161. [pasted: java.lang.NoClassDefFoundError: javax/websocket/Session]
162. show me the command of connect wbesocket
163. I think the websocket should be persistent?
164. what is @onopen?
165. scheduleConnect(resortID, 0, 1000); why 0?
166. in main, 10 connection are created by scheduelconnect, but not in seperate threads
167. oh I see, scheduler is the threadpoolexecutor
168. container.connectToServer(new SubscribeClient(resortID), URI.create(URL + resortID)); why need a new instance?
169. long backoffMs = Math.min(MAX_BACKOFF_MS, 1000L \<\< Math.min(attempt, 5)); ???
170. those counters are shared by all instances?
171. [pasted: shutdown/close-sessions snippet] the threads are alredy terminated, why close the session after?
172. so there are 10 session, initaited by a thread pool of 10 threads, and run on that 10 threads?
173. if the thread pool is only for connect, why bother using a thread pool?
174. Thread.sleep(DEFAULT_RUN_SECONDS * 1000L); which thread is this?
175. The WebSocket library's threads , where are they? I only see one threadpoolexector
176. that is why the main thread need to wait, the ws swssion threads are running somewhere else
177. another purposr of thread pool is that, there are ten threads, mostly idle after the initian conenction, however, when the session thread received a error, the pool therad will be get a new "job" which is to reconenct, right?
178. instead, i can choose not to use the thread pool, when onclose, it can call a method to reconnect , just pass the session ID, so no 10 threads witing there to reconnect their "own" session
179. so the threada in pool are not bounded, who ever get a job first will execute it
180. the 10 threads are fired one by one by the for loop in main
181. scheduler.schedule(() -> connect(resortID, attempt), delayMs, TimeUnit.MILLISECONDS); this take the job from whichever session, and allocate to whichever thread, it is not bounded
182. on close is not a static method, so it is called by the instance whose session is closed, but scheduler is static, it is shared
183. [pasted: client output] why the latecy number is so huge?
184. edit that endpoint, to show current time
185. I have deployed the .war to ec2, I can see throught the broswer, but when I run SwipeClient in ItelliJ, it shows resort 5 connect failed (attempt 1): javax.websocket.DeploymentException: Connection failed.
186. [pasted: performance-metrics requirements (ingest path, subscribe path, Little's Law)] check my SwipeClient and SubscribeClient, against these requirements
187. fix for me, based on my current code
188. so only made some change to the two client class, no change to the server
189. [pasted: Part 3 performance-analysis requirements (per-swipe metrics, CSV, push latency, statistics)] are these covered?
190. do it
191. check my code, I made some change. why the swipeclient call subscribeclient.start() but no connections?
192. [pasted: SwipeClient console output] this is the result
193. does current data supprt making a throughput-over-time chart?
194. I have created a github repo, how to connect to lcoal fir
195. should it be git remote somthing?
196. I mean, I have alrady created a github repo on website, I want to link to my local folder, how
197. Create a line chart of ingest throughput over time (swipes/second in 10-second buckets).
198. wirte a simple readme in submission/assigment1/README.md with clear run instructions
199. make the run client part simpler, also mention to change the URL to ec2 instance ip address
200. delete what is in folder part
201. delete "what is in this folder" part
202. I think my current code may not reach the limit of throughput. in swipe client, I qrite message to a blocking queue, and let 100 threads to post. I have no idea 1) if 100 threads is enough to test out the limit of throughput, 2) maybe the bolocking queue is the bottle neck rather than the throughtput of server
203. check my swipeclient, it is not ingesting
204. did I rewrite csv or append?
205. I have new data, in .csv and assignment1output.txt, redo throughput-time graph, and update readme ifnecessary
206. my ec2 usage are CPU ~30% ,memory!30%, so what could the bottle neck be?
207. later I observe that the cpu is 180%, so CPU is the bottel neck?
208. add a gitignre
209. I pushed, but in my github repo, the .DS_store are still there
210. how to remove the .DS_store that I have pushed?
211. [pasted: design-document requirements (architecture diagram, major classes, threading model, WebSocket management, Little's Law)] draft a design document, I have created the file. including a short analysis on the output
212. maybe draw the digrams using mermaid syntax?
213. how to conver to pdf?
214. add the proj repo URL at front, as a part of the docuemnt
215. I can see the .md redered correctly in github webpage, but cmd+p doesn't work
216. github chrome page print doesn't, another way?
217. check my current design document. make the threading model two horizontal gragh, now it is a horizontal plus a vertical, waste space
218. mvoe the result analysis part to /result, create a .md. then the design_document is purely design document. also, the two graphs in threading model part cann't render correctly when export to pdf
219. in designment, major classes part, make is text, not table
220. my gitignore is not ignoring itself?
221. [pasted: Test Results requirements (screenshots, chart, EC2 evidence, WebSocket screenshot)] I think this part should also be in the design document, after current content. the scren shots are in results/, combine to .md together with the content form result analysis.md
222. scan my source code, assess how difficult will it be, to migrate from Java to C++
223. not doable
224. correntmap vs synchronized map
225. due to mutiple locks?
226. when to synchronized map over cocurrent map>
227. concurrent map is typically slow due to overhead?
228. extract my AI prompts to a .md to submissio/assighment1
229. just make a plain list as .md
