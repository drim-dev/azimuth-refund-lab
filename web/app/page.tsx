import { RefundLab } from "@/components/refund-lab";

export const dynamic = "force-dynamic";

export default function Home() {
  return <RefundLab unsafeWorkerMode={process.env.REFUND_DEMO_UNSAFE_WORKER === "true"} />;
}
