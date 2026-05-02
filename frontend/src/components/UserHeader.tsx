import { auth, signOut } from "@/auth";
import Image from "next/image";

export default async function UserHeader() {
  const session = await auth();
  if (!session?.user) return null;

  return (
    <header className="flex items-center justify-between px-6 py-4 bg-white shadow-sm">
      <h1 className="text-xl font-bold text-emerald-700">🌱 Wakaba</h1>
      <div className="flex items-center gap-4">
        {session.user.image && (
          <Image
            src={session.user.image}
            alt={session.user.name ?? "User"}
            width={32}
            height={32}
            className="rounded-full"
          />
        )}
        <span className="text-sm font-medium text-gray-700">{session.user.name}</span>
        <form
          action={async () => {
            "use server";
            await signOut({ redirectTo: "/" });
          }}
        >
          <button
            type="submit"
            className="text-sm text-gray-500 hover:text-gray-800 transition"
          >
            ログアウト
          </button>
        </form>
      </div>
    </header>
  );
}
