package me.sudodios.orangeplayer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import org.jetbrains.skia.Rect
import org.jetbrains.skia.skottie.Animation
import org.jetbrains.skia.sksg.InvalidationController

@Composable
fun PlayAnimationView(
    modifier : Modifier,
    color: Color = Color.White,
    play : Boolean
) {

    val source = """
{"nm":"morph-play-pause","ddd":0,"h":500,"w":500,"meta":{"g":"LottieFiles AE 3.0.2"},"layers":[{"ty":4,"nm":".primary.design","sr":1,"st":13,"op":351,"ip":30,"hd":false,"cl":"primary design","ddd":0,"bm":0,"hasMask":false,"ao":0,"ks":{"a":{"a":0,"k":[333.379,250,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6},"sk":{"a":0,"k":0},"p":{"a":0,"k":[333.379,250,0],"ix":2},"r":{"a":0,"k":0,"ix":10},"sa":{"a":0,"k":0},"o":{"a":0,"k":100,"ix":11}},"ef":[],"shapes":[{"ty":"gr","bm":0,"hd":false,"mn":"ADBE Vector Group","nm":"Group 1","ix":1,"cix":2,"np":2,"it":[{"ty":"sh","bm":0,"hd":false,"mn":"ADBE Vector Shape - Group","nm":"Path 1","ix":1,"d":1,"ks":{"a":0,"k":{"c":true,"i":[[11.458,0],[0,0],[0,11.458],[0,0],[-11.458,0],[0,0],[0,-11.458],[0,0]],"o":[[0,0],[-11.458,0],[0,0],[0,-11.458],[0,0],[11.458,0],[0,0],[0,11.458]],"v":[[20.833,145.833],[-20.833,145.833],[-41.667,125],[-41.667,-125],[-20.833,-145.833],[20.833,-145.833],[41.667,-125],[41.667,125]]},"ix":2}},{"ty":"fl","bm":0,"hd":false,"mn":"ADBE Vector Graphic - Fill","nm":"Fill 1","c":{"a":0,"k":[${color.red},${color.green},${color.blue}],"ix":4},"r":1,"o":{"a":0,"k":100,"ix":5}},{"ty":"tr","a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"sk":{"a":0,"k":0,"ix":4},"p":{"a":0,"k":[333.379,250],"ix":2},"r":{"a":0,"k":0,"ix":6},"sa":{"a":0,"k":0,"ix":5},"o":{"a":0,"k":100,"ix":7}}]}],"ind":1},{"ty":4,"nm":".primary.design","sr":1,"st":-6,"op":347,"ip":16,"hd":false,"cl":"primary design","ddd":0,"bm":0,"hasMask":false,"ao":0,"ks":{"a":{"a":0,"k":[166.713,250,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6},"sk":{"a":0,"k":0},"p":{"a":0,"k":[166.713,250,0],"ix":2},"r":{"a":0,"k":0,"ix":10},"sa":{"a":0,"k":0},"o":{"a":0,"k":100,"ix":11}},"ef":[],"shapes":[{"ty":"gr","bm":0,"hd":false,"mn":"ADBE Vector Group","nm":"Group 2","ix":1,"cix":2,"np":2,"it":[{"ty":"sh","bm":0,"hd":false,"mn":"ADBE Vector Shape - Group","nm":"Path 1","ix":1,"d":1,"ks":{"a":1,"k":[{"o":{"x":0.167,"y":0},"i":{"x":0.833,"y":1},"s":[{"c":true,"i":[[5.814,0],[0,0],[0,5.814],[0,0],[-5.814,0],[0,0],[0,-5.814],[0,0]],"o":[[0,0],[-5.814,0],[0,0],[0,-5.814],[0,0],[5.814,0],[0,0],[0,5.814]],"v":[[-111.429,147],[-126.571,147],[-137.143,136.429],[-137.143,-137.429],[-126.571,-148],[-111.429,-148],[-100.857,-137.429],[-100.857,136.429]]}],"t":16},{"s":[{"c":true,"i":[[11.458,0],[0,0],[0,11.458],[0,0],[-11.458,0],[0,0],[0,-11.458],[0,0]],"o":[[0,0],[-11.458,0],[0,0],[0,-11.458],[0,0],[11.458,0],[0,0],[0,11.458]],"v":[[20.833,145.833],[-20.833,145.833],[-41.667,125],[-41.667,-125],[-20.833,-145.833],[20.833,-145.833],[41.667,-125],[41.667,125]]}],"t":30}],"ix":2}},{"ty":"fl","bm":0,"hd":false,"mn":"ADBE Vector Graphic - Fill","nm":"Fill 1","c":{"a":0,"k":[${color.red},${color.green},${color.blue}],"ix":4},"r":1,"o":{"a":0,"k":100,"ix":5}},{"ty":"tr","a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"sk":{"a":0,"k":0,"ix":4},"p":{"a":0,"k":[166.713,250],"ix":2},"r":{"a":0,"k":0,"ix":6},"sa":{"a":0,"k":0,"ix":5},"o":{"a":0,"k":100,"ix":7}}]}],"ind":2},{"ty":4,"nm":".primary.design","sr":1,"st":-30,"op":2,"ip":0,"hd":false,"cl":"primary design","ddd":0,"bm":0,"hasMask":false,"ao":0,"ks":{"a":{"a":0,"k":[0,0,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6},"sk":{"a":0,"k":0},"p":{"a":0,"k":[272.202,250.003,0],"ix":2},"r":{"a":0,"k":0,"ix":10},"sa":{"a":0,"k":0},"o":{"a":0,"k":100,"ix":11}},"ef":[],"shapes":[{"ty":"gr","bm":0,"hd":false,"mn":"ADBE Vector Group","nm":"Group 1","ix":1,"cix":2,"np":2,"it":[{"ty":"sh","bm":0,"hd":false,"mn":"ADBE Vector Shape - Group","nm":"Path 1","ix":1,"d":1,"ks":{"a":0,"k":{"c":true,"i":[[13.125,8.125],[0,0],[0,-16.458],[0,0],[-13.75,8.542],[0,0]],"o":[[0,0],[-13.75,-8.542],[0,0],[0,16.25],[0,0],[13.333,-8.333]],"v":[[116.549,-17.71],[-94.7,-147.293],[-126.367,-129.376],[-126.367,129.581],[-94.7,147.289],[116.341,17.915]]},"ix":2}},{"ty":"fl","bm":0,"hd":false,"mn":"ADBE Vector Graphic - Fill","nm":"Fill 1","c":{"a":0,"k":[${color.red},${color.green},${color.blue}],"ix":4},"r":1,"o":{"a":0,"k":100,"ix":5}},{"ty":"tr","a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"sk":{"a":0,"k":0,"ix":4},"p":{"a":0,"k":[0,0],"ix":2},"r":{"a":0,"k":0,"ix":6},"sa":{"a":0,"k":0,"ix":5},"o":{"a":0,"k":100,"ix":7}}]}],"ind":3},{"ty":4,"nm":".primary.design","sr":1,"st":0,"op":30,"ip":1,"hd":false,"cl":"primary design","ddd":0,"bm":0,"hasMask":false,"ao":0,"ks":{"a":{"a":0,"k":[-36,0,0],"ix":1},"s":{"a":0,"k":[100,100,100],"ix":6},"sk":{"a":0,"k":0},"p":{"a":1,"k":[{"o":{"x":0.333,"y":0},"i":{"x":0.667,"y":1},"s":[235.599,250.012,0],"t":1,"ti":[-8.333,0,0],"to":[8.333,0,0]},{"s":[285.599,250.012,0],"t":30}],"ix":2},"r":{"a":1,"k":[{"o":{"x":0.333,"y":0},"i":{"x":0.667,"y":1},"s":[0],"t":1},{"s":[59],"t":30}],"ix":10},"sa":{"a":0,"k":0},"o":{"a":0,"k":100,"ix":11}},"ef":[],"shapes":[{"ty":"gr","bm":0,"hd":false,"mn":"ADBE Vector Group","nm":"Group 1","ix":1,"cix":2,"np":3,"it":[{"ty":"sh","bm":0,"hd":false,"mn":"ADBE Vector Shape - Group","nm":"Path 1","ix":1,"d":1,"ks":{"a":1,"k":[{"o":{"x":0.333,"y":0},"i":{"x":0.833,"y":1},"s":[{"c":true,"i":[[13.519,8.188],[0,0],[0,-16.586],[0,0],[-0.56,-1.868],[-4.753,-2.593],[-7.075,4.3],[0,0]],"o":[[0,0],[-14.163,-8.608],[0,0],[0,2.102],[1.624,5.416],[6.381,3.481],[0,0],[13.734,-8.398]],"v":[[117.048,-17.847],[-93.543,-148.435],[-126.16,-130.38],[-126.16,130.586],[-125.296,136.555],[-115.144,148.888],[-93.543,148.432],[116.833,18.054]]}],"t":1},{"o":{"x":0.167,"y":0},"i":{"x":0.833,"y":1},"s":[{"c":true,"i":[[13.519,8.188],[0,0],[1.274,-9.582],[-6.075,-53.266],[-19.294,-26.773],[-42.164,-7.445],[-24.719,10.203],[0,0]],"o":[[0,0],[-15.711,-8.972],[0,0],[2.304,20.202],[36.909,51.217],[22.141,3.01],[55.601,-22.951],[7.929,-13.561]],"v":[[120.047,-17.847],[-97.569,-148.391],[-129.634,-134.799],[-154.469,-36.776],[-128.447,31.279],[-19.098,95.48],[51.018,84.753],[126.651,17.06]]}],"t":16},{"o":{"x":0.167,"y":0},"i":{"x":0.667,"y":1},"s":[{"c":true,"i":[[13.519,8.188],[0,0],[1.975,-5.734],[-1.789,-19.712],[-13.381,-12.832],[-37.07,-16.608],[-15.347,8.519],[0,0]],"o":[[0,0],[-16.561,-9.172],[0,0],[-0.401,17.521],[32.475,28.631],[17.566,7.289],[17.739,-7.322],[7.5,-12.448]],"v":[[120.047,-17.847],[-97.584,-148.367],[-129.345,-137.228],[-152.145,-80.314],[-136.433,-36.491],[37.284,70.765],[88.291,67.228],[130.397,16.515]]}],"t":23},{"s":[{"c":true,"i":[[13.519,8.188],[0,0],[2.303,-3.931],[0,0],[-10.61,-6.3],[-34.683,-20.901],[-5.998,8.366],[0,0]],"o":[[0,0],[-16.96,-9.266],[0,0],[-8.46,14.071],[30.397,18.049],[15.423,9.294],[0,0],[7.3,-11.926]],"v":[[110.957,-16.31],[-96.61,-140.766],[-128.229,-130.776],[-145.353,-101.953],[-138.594,-68.974],[70.251,56.612],[101.814,51.984],[123.062,17.797]]}],"t":30}],"ix":2}},{"ty":"fl","bm":0,"hd":false,"mn":"ADBE Vector Graphic - Fill","nm":"Fill 1","c":{"a":0,"k":[${color.red},${color.green},${color.blue}],"ix":4},"r":1,"o":{"a":0,"k":100,"ix":5}},{"ty":"st","bm":0,"hd":false,"mn":"ADBE Vector Graphic - Stroke","nm":"Stroke 1","lc":2,"lj":2,"ml":1,"o":{"a":0,"k":100,"ix":4},"w":{"a":0,"k":0,"ix":5},"c":{"a":0,"k":[${color.red},${color.green},${color.blue}],"ix":3}},{"ty":"tr","a":{"a":0,"k":[0,0],"ix":1},"s":{"a":0,"k":[100,100],"ix":3},"sk":{"a":0,"k":0,"ix":4},"p":{"a":0,"k":[0,0],"ix":2},"r":{"a":0,"k":0,"ix":6},"sa":{"a":0,"k":0,"ix":5},"o":{"a":0,"k":100,"ix":7}}]}],"ind":4}],"v":"4.8.0","fr":60,"op":31,"ip":0,"assets":[]}
"""

    val animation = Animation.makeFromString(source)

    val animate = animateFloatAsState(
        targetValue = if (play) animation.duration else 0f,
        animationSpec = tween(550)
    )

    val invalidationController = remember { InvalidationController() }
    animation.seekFrameTime(animate.value, invalidationController)

    Canvas(modifier) {
        drawIntoCanvas {
            animation.render(
                canvas = it.nativeCanvas,
                dst = Rect.makeWH(size.width, size.height)
            )
        }
    }

}