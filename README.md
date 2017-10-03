# atum-jvcp
java virtual card proxy for sharing of conditional access modules<br />
<br />
All current card proxy servers utilized a thread-per client model. This is not only un-effficent but completly unscalable. JVCP is attempting to fill that gap by providing a newcamd / cccam CAM proxy implementation using java NIO.<br />
This will utilize an event driven system capable of using multiple threads to access a common cache. This will remove the low user limitation of CSP and oscam. Allowing administrators to scale there systems to several thousand users.<br />

•	High-speed networking implementation to achieve a transaction rate exceeding fifty thousand transactions per second while maintaining a high TCP connection count.<br />
•	Cross protocol communication between four unique protocols. (CCcam, Newcamd, GHttp, Camd35).<br />
<br />
Libraries used:<br />
•	Netty networking library.<br />
•	NIO<br />
•	Gson<br />
•	Log4j<br />
<br />
