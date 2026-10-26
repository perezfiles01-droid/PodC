import React from "react";
import { registerRoot, Composition, AbsoluteFill, useCurrentFrame, spring, useVideoConfig } from "remotion";

// Smoke test: a bouncy caption pop-in (proves Remotion renders in this environment).
const Pop = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const s = spring({ frame, fps, config: { damping: 8 } });
  return (
    <AbsoluteFill style={{ backgroundColor: "#87CEEB", justifyContent: "center", alignItems: "center" }}>
      <div style={{ fontSize: 120, fontWeight: 800, color: "white", transform: `scale(${s})`,
                    WebkitTextStroke: "6px black" }}>"IT'S STUCK!"</div>
    </AbsoluteFill>
  );
};

registerRoot(() => (
  <Composition id="Pop" component={Pop} durationInFrames={60} fps={30} width={1080} height={1920} />
));
