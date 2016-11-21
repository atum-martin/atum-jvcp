# atum-jvcp
java virtual card proxy for sharing of conditional access modules

All current card proxy servers utilized a thread-per client model. This is not only un-effficent but completly unscalable. JVCP is attempting to fill that gap by providing a newcamd / cccam CAM proxy implementation using java NIO.
This will utilize an event driven system capable of using multiple threads to access a common cache. This will remove the low user limitation of CSP and oscam. Allowing administrators to scale there systems to several thousand users.
