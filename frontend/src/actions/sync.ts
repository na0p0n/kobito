"use server";
import { auth } from "@/auth";
import { api } from "@/lib/api";
import { revalidatePath } from "next/cache";

export async function syncContributions() {
  const session = await auth();
  if (!session?.user?.id) throw new Error("Unauthorized");
  const accessToken = (session as any).accessToken as string;
  await api.sync.trigger(session.user.id, accessToken);
  revalidatePath("/dashboard");
}
